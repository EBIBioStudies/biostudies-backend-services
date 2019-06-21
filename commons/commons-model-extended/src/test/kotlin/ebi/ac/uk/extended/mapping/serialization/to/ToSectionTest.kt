package ebi.ac.uk.extended.mapping.serialization.to

import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLibraryFile
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
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
    @MockK val libraryFile: LibraryFile,
    @MockK val attribute: Attribute,
    @MockK val file: File,
    @MockK val fileTable: FilesTable,
    @MockK val link: Link,
    @MockK val linkTable: LinksTable,
    @MockK val subsection: Section,
    @MockK val sectionTable: SectionsTable,
    @MockK val extLibraryFile: ExtLibraryFile,
    @MockK val extAttribute: ExtAttribute,
    @MockK val extFile: ExtFile,
    @MockK val extFileTable: ExtFileTable,
    @MockK val extLink: ExtLink,
    @MockK val extLinkTable: ExtLinkTable,
    @MockK val extSubsection: ExtSection,
    @MockK val extSectionTable: ExtSectionTable
) {

    private val section = ExtSection(
        type = "type",
        accNo = "accNo",
        libraryFile = extLibraryFile,
        attributes = listOf(extAttribute),
        files = listOf(left(extFile), right(extFileTable)),
        links = listOf(left(extLink), right(extLinkTable)),
        sections = listOf(left(extSubsection), right(extSectionTable))
    )

    @Test
    fun toSection() {
        mockkStatic(
            TO_ATTRIBUTE_EXTENSIONS,
            TO_FILE_EXTENSIONS,
            TO_LIBRARY_FILE_EXTENSIONS,
            TO_LINK_EXTENSIONS,
            TO_TABLE_EXTENSIONS,
            TO_SECTION_EXTENSIONS)

        every { extAttribute.toAttribute() } returns attribute
        every { extFile.toFile() } returns file
        every { extLibraryFile.toLibraryFile() } returns libraryFile
        every { extLink.toLink() } returns link
        every { extFileTable.toTable() } returns fileTable
        every { extLinkTable.toTable() } returns linkTable
        every { extSectionTable.toTable() } returns sectionTable
        every { extLibraryFile.toLibraryFile() } returns libraryFile
        every { extSubsection.toSubSection() } returns subsection

        val sectionResult = section.toSection()
        assertThat(sectionResult.accNo).isEqualTo(section.accNo)
        assertThat(sectionResult.type).isEqualTo(section.type)
        assertThat(sectionResult.libraryFile).isEqualTo(libraryFile)
        assertThat(sectionResult.attributes.first()).isEqualTo(attribute)
        assertThat(sectionResult.files.first()).isEqualTo(left(file))
        assertThat(sectionResult.files.second()).isEqualTo(right(fileTable))
        assertThat(sectionResult.links.first()).isEqualTo(left(link))
        assertThat(sectionResult.links.second()).isEqualTo(right(linkTable))
        assertThat(sectionResult.sections.first()).isEqualTo(left(subsection))
        assertThat(sectionResult.sections.second()).isEqualTo(right(sectionTable))
    }
}
