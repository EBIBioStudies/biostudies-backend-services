package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.base.Either.Companion.left
import ebi.ac.uk.base.Either.Companion.right
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.util.collections.second
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ebi.ac.uk.asserts.assertThat as assertEither

@ExtendWith(MockKExtension::class)
class ToSectionMapperTest(
    @MockK val fileList: FileList,
    @MockK val attribute: Attribute,
    @MockK val file: BioFile,
    @MockK val fileTable: FilesTable,
    @MockK val link: Link,
    @MockK val linkTable: LinksTable,
    @MockK val extFileList: ExtFileList,
    @MockK val extAttribute: ExtAttribute,
    @MockK val extFile: ExtFile,
    @MockK val extFileTable: ExtFileTable,
    @MockK val extLink: ExtLink,
    @MockK val extLinkTable: ExtLinkTable,
) {
    private val subExtSection = ExtSection(type = "subtype", accNo = "accNo1")
    private val subSection = Section(type = "subtype", accNo = "accNo1")
    private val section =
        ExtSection(
            type = "type",
            accNo = "accNo",
            fileList = extFileList,
            attributes = listOf(extAttribute),
            files = listOf(left(extFile), right(extFileTable)),
            links = listOf(left(extLink), right(extLinkTable)),
            sections = listOf(left(subExtSection), right(ExtSectionTable(listOf(subExtSection)))),
        )
    private val toFileListMapper = mockk<ToFileListMapper>()
    private val testInstance = ToSectionMapper(toFileListMapper)

    @Test
    fun toSection() =
        runTest {
            mockkStatic(
                TO_ATTRIBUTE_EXTENSIONS,
                TO_FILE_EXTENSIONS,
                TO_LINK_EXTENSIONS,
                TO_TABLE_EXTENSIONS,
            ) {
                every { extAttribute.toAttribute() } returns attribute
                every { extFile.toFile() } returns file
                coEvery { toFileListMapper.convert(extFileList) } returns fileList
                every { extLink.toLink() } returns link
                every { extFileTable.toTable() } returns fileTable
                every { extLinkTable.toTable() } returns linkTable

                val sectionResult = testInstance.convert(section, false)

                assertThat(sectionResult.accNo).isEqualTo(section.accNo)
                assertThat(sectionResult.type).isEqualTo(section.type)
                assertThat(sectionResult.fileList).isEqualTo(fileList)
                assertThat(sectionResult.attributes.first()).isEqualTo(attribute)
                assertThat(sectionResult.files.first()).isEqualTo(left(file))
                assertThat(sectionResult.files.second()).isEqualTo(right(fileTable))
                assertThat(sectionResult.links.first()).isEqualTo(left(link))
                assertThat(sectionResult.links.second()).isEqualTo(right(linkTable))
                assertEither(sectionResult.sections.first()).hasLeftValueSatisfying {
                    assertThat(it).isEqualTo(
                        subSection,
                    )
                }
                assertEither(sectionResult.sections.second()).hasRightValueSatisfying {
                    assertThat(it.elements.first()).isEqualTo(subSection)
                }
            }
        }
}
