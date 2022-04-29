package ebi.ac.uk.model.extensions

import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.sectionsTable
import ebi.ac.uk.model.BioFile
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
        assertThat(files.first()).isEqualTo(BioFile("File1.txt"))
        assertThat(files.second()).isEqualTo(BioFile("File2.txt"))
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
}
