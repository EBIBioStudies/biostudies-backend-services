package uk.ac.ebi.biostd.model.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.dsl.file
import uk.ac.ebi.biostd.dsl.filesTable
import uk.ac.ebi.biostd.dsl.section
import uk.ac.ebi.biostd.dsl.sectionsTable
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.util.collections.second

class SectionExtTest {
    @Test
    fun `all section files`() {
        val section =
            section("Study") {
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
        val section =
            section("Study") {
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
