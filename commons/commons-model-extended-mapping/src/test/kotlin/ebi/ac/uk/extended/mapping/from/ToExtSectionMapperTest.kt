package ebi.ac.uk.extended.mapping.from

import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.SectionFields
import ebi.ac.uk.util.collections.second
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ebi.ac.uk.asserts.assertThat as assertEither

@ExtendWith(MockKExtension::class)
class ToExtSectionMapperTest(
    @MockK val fileSource: FilesSource,
    @MockK val fileList: FileList,
    @MockK val attribute: Attribute,
    @MockK val file: File,
    @MockK val fileTable: FilesTable,
    @MockK val link: Link,
    @MockK val linkTable: LinksTable,
    @MockK val extFileList: ExtFileList,
    @MockK val extAttribute: ExtAttribute,
    @MockK val fileListAttribute: ExtAttribute,
    @MockK val extFile: ExtFile,
    @MockK val extFileTable: ExtFileTable,
    @MockK val extLink: ExtLink,
    @MockK val extLinkTable: ExtLinkTable
) {
    private val subSection = Section(type = "subtype", accNo = "accNo1")
    private val subExtSection = ExtSection(type = "subtype", accNo = "accNo1")
    private val section = Section(
        type = "type",
        accNo = "accNo",
        fileList = fileList,
        attributes = listOf(attribute),
        files = mutableListOf(left(file), right(fileTable)),
        links = mutableListOf(left(link), right(linkTable)),
        sections = mutableListOf(left(subSection), right(SectionsTable(listOf(subSection))))
    )
    private val toExtFileListMapper: ToExtFileListMapper = mockk()
    private val testInstance = ToExtSectionMapper(toExtFileListMapper)

    @Test
    fun toExtSection() {
        mockkStatic(
            TO_EXT_ATTRIBUTE_EXTENSIONS,
            TO_EXT_FILE_EXTENSIONS,
            TO_EXT_LINK_EXTENSIONS,
            TO_EXT_TABLE_EXTENSIONS
        ) {
            every { attribute.name } returns "attr1"
            every { fileListAttribute.name } returns SectionFields.FILE_LIST.value
            every { attribute.toExtAttribute() } returns extAttribute
            every { file.toExtFile(fileSource) } returns extFile
            every { toExtFileListMapper.convert(SUB_ACC, SUB_VERSION, fileList, fileSource) } returns extFileList
            every { link.toExtLink() } returns extLink
            every { fileTable.toExtTable(fileSource) } returns extFileTable
            every { linkTable.toExtTable() } returns extLinkTable

            val sectionResult = testInstance.convert(SUB_ACC, SUB_VERSION, section, fileSource)

            assertThat(sectionResult.accNo).isEqualTo(section.accNo)
            assertThat(sectionResult.type).isEqualTo(section.type)
            assertThat(sectionResult.fileList).isEqualTo(extFileList)
            assertThat(sectionResult.attributes.first()).isEqualTo(extAttribute)
            assertThat(sectionResult.files.first()).isEqualTo(left(extFile))
            assertThat(sectionResult.files.second()).isEqualTo(right(extFileTable))
            assertThat(sectionResult.links.first()).isEqualTo(left(extLink))
            assertThat(sectionResult.links.second()).isEqualTo(right(extLinkTable))
            assertEither(sectionResult.sections.first()).hasLeftValueSatisfying {
                assertThat(it).isEqualTo(subExtSection)
            }
            assertEither(sectionResult.sections.second()).hasRightValueSatisfying {
                assertThat(it).isEqualTo(ExtSectionTable(listOf(subExtSection)))
            }
        }
    }

    companion object {
        val SUB_ACC = "sub-acc"
        val SUB_VERSION = 10
    }
}
