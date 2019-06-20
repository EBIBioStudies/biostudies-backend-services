package ebi.ac.uk.extended.mapping.serialization.from

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
import ebi.ac.uk.utils.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ToExtSectionTest(
    @MockK val fileSource: FilesSource,
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

    private val section = Section(
        type = "type",
        accNo = "accNo",
        libraryFile = libraryFile,
        attributes = listOf(attribute),
        files = listOf(left(file), right(fileTable)).toMutableList(),
        links = listOf(left(link), right(linkTable)).toMutableList(),
        sections = listOf(left(subsection), right(sectionTable)).toMutableList()
    )

    @Test
    fun toExtSection() {
        mockkStatic(
            TO_EXT_ATTRIBUTE_EXTENSIONS,
            TO_EXT_FILE_EXTENSIONS,
            TO_EXT_LIBRARY_FILE_EXTENSIONS,
            TO_EXT_LINK_EXTENSIONS,
            TO_EXT_TABLE_EXTENSIONS,
            TO_EXT_SECTION_EXTENSIONS)

        every { attribute.toExtAttribute() } returns extAttribute
        every { file.toExtFile(fileSource) } returns extFile
        every { libraryFile.toExtLibraryFile(fileSource) } returns extLibraryFile
        every { link.toExtLink() } returns extLink
        every { fileTable.toExtTable(fileSource) } returns extFileTable
        every { linkTable.toExtTable() } returns extLinkTable
        every { sectionTable.toExtTable(fileSource) } returns extSectionTable
        every { libraryFile.toExtLibraryFile(fileSource) } returns extLibraryFile
        every { subsection.toExtSubSection(fileSource) } returns extSubsection

        val sectionResult = section.toExtSection(fileSource)
        assertThat(sectionResult.accNo).isEqualTo(section.accNo)
        assertThat(sectionResult.type).isEqualTo(section.type)
        assertThat(sectionResult.libraryFile).isEqualTo(extLibraryFile)
        assertThat(sectionResult.attributes.first()).isEqualTo(extAttribute)
        assertThat(sectionResult.files.first()).isEqualTo(left(extFile))
        assertThat(sectionResult.files.second()).isEqualTo(right(extFileTable))
        assertThat(sectionResult.links.first()).isEqualTo(left(extLink))
        assertThat(sectionResult.links.second()).isEqualTo(right(extLinkTable))
        assertThat(sectionResult.sections.first()).isEqualTo(left(extSubsection))
        assertThat(sectionResult.sections.second()).isEqualTo(right(extSectionTable))
    }
}
