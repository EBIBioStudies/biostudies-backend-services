package ebi.ac.uk.model.extensions

import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.sectionsTable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Section
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SectionExtTest {
    @Test
    fun `all section files`() {
        val section = section("Study") {
            file("File1.txt")
            filesTable { file("File2.txt") }
        }
        val files = section.allFiles()

        assertThat(files).hasSize(2)
        assertThat(files.first()).isEqualTo(File("File1.txt"))
        assertThat(files.second()).isEqualTo(File("File2.txt"))
    }

    @Test
    fun `all sections`() {
        val section = section("Study") {
            section("Subsection 1") { accNo = "SUB-SECT-001" }
            sectionsTable {
                section("Subsection 2") { accNo = "SUB-SECT-002" }
            }
        }
        val allSubsections = section.allSections()

        assertThat(allSubsections).hasSize(2)
        assertThat(allSubsections.first()).isEqualTo((Section("Subsection 1", "SUB-SECT-001")))
        assertThat(allSubsections.second()).isEqualTo((Section("Subsection 2", "SUB-SECT-002")))
    }

    @Test
    fun `file list name`() {
        val section = section("Study") { }
        section.fileListName = "FileList.tsv"

        assertThat(section.fileListName).isEqualTo("FileList.tsv")
        assertThat(section.attributes).isEqualTo(listOf(Attribute("File List", "FileList.tsv")))
    }

    @Test
    fun `section title`() {
        val section = section("Study") { }
        section.title = "Section Title"

        assertThat(section.title).isEqualTo("Section Title")
        assertThat(section.attributes).isEqualTo(listOf(Attribute("Title", "Section Title")))
    }
}
