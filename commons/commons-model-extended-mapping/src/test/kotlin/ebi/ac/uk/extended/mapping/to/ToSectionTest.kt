package ebi.ac.uk.extended.mapping.to

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
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.util.collections.second
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ToSectionTest(
    @MockK val fileList: FileList,
    @MockK val attribute: Attribute,
    @MockK val file: File,
    @MockK val fileTable: FilesTable,
    @MockK val link: Link,
    @MockK val linkTable: LinksTable,
    @MockK val sectionTable: SectionsTable,
    @MockK val extFileList: ExtFileList,
    @MockK val extAttribute: ExtAttribute,
    @MockK val extFile: ExtFile,
    @MockK val extFileTable: ExtFileTable,
    @MockK val extLink: ExtLink,
    @MockK val extLinkTable: ExtLinkTable,
    @MockK val extSectionTable: ExtSectionTable
) {
    private val subSection = ExtSection(type = "subtype", accNo = "accNo1")
    private val section = ExtSection(
        type = "type",
        accNo = "accNo",
        fileList = extFileList,
        attributes = listOf(extAttribute),
        files = listOf(left(extFile), right(extFileTable)),
        links = listOf(left(extLink), right(extLinkTable)),
        sections = listOf(left(subSection), right(extSectionTable))
    )

    @Test
    fun toSection() {
        mockkStatic(
            TO_ATTRIBUTE_EXTENSIONS,
            TO_FILE_EXTENSIONS,
            TO_LIBRARY_FILE_EXTENSIONS,
            TO_LINK_EXTENSIONS,
            TO_TABLE_EXTENSIONS,
            TO_SECTION_EXTENSIONS) {

            every { extAttribute.toAttribute() } returns attribute
            every { extFile.toFile() } returns file
            every { extFileList.toFileList() } returns fileList
            every { extLink.toLink() } returns link
            every { extFileTable.toTable() } returns fileTable
            every { extLinkTable.toTable() } returns linkTable
            every { extSectionTable.toTable() } returns sectionTable
            every { extFileList.toFileList() } returns fileList

            val sectionResult = section.toSection()
            assertThat(sectionResult.accNo).isEqualTo(section.accNo)
            assertThat(sectionResult.type).isEqualTo(section.type)
            assertThat(sectionResult.fileList).isEqualTo(fileList)
            assertThat(sectionResult.attributes.first()).isEqualTo(attribute)
            assertThat(sectionResult.files.first()).isEqualTo(left(file))
            assertThat(sectionResult.files.second()).isEqualTo(right(fileTable))
            assertThat(sectionResult.links.first()).isEqualTo(left(link))
            assertThat(sectionResult.links.second()).isEqualTo(right(linkTable))
            assertThat(sectionResult.sections.second()).isEqualTo(right(sectionTable))
            sectionResult.sections.first().mapLeft {
                assertThat(it.type).isEqualTo("subtype")
                assertThat(it.accNo).isEqualTo("accNo1")
            }
        }
    }
}
