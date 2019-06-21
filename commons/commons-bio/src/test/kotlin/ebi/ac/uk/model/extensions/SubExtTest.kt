package ebi.ac.uk.model.extensions

import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubExtTest {
    @Test
    fun `attach to`() {
        val submission = submission("ABC-123") {}
        submission.attachTo = "ParentProject"

        assertThat(submission.attachTo).isEqualTo("ParentProject")
        assertExtendedAttribute(submission, SubFields.ATTACH_TO, "ParentProject")
    }

    @Test
    fun `release date`() {
        val submission = submission("ABC-123") {}
        submission.releaseDate = "2015-02-20"

        assertExtendedAttribute(submission, SubFields.RELEASE_DATE, "2015-02-20")
    }

    @Test
    fun title() {
        val submission = submission("ABC-123") {}
        submission.title = "Title"

        assertThat(submission.title).isEqualTo("Title")
        assertExtendedAttribute(submission, SubFields.TITLE, "Title")
    }

    @Test
    fun `root path`() {
        val submission = submission("ABC-123") {}
        submission.rootPath = "/some/path"

        assertThat(submission.rootPath).isEqualTo("/some/path")
        assertExtendedAttribute(submission, SubFields.ROOT_PATH, "/some/path")
    }

    @Test
    fun `all submission files`() {
        val submission = submission("ABC-123") {
            section("Study") {
                file("File1.txt")

                section("Data") {
                    filesTable {
                        file("DataFile1.csv")
                    }
                }
            }
        }
        val allFiles = submission.allFiles()

        assertThat(allFiles).hasSize(2)
        assertThat(allFiles.first()).isEqualTo(File("File1.txt"))
        assertThat(allFiles.second()).isEqualTo(File("DataFile1.csv"))
    }

    @Test
    fun `get section by type`() {
        val submission = submission("ABC-123") {
            section("Study") {
                section("Data") { accNo = "DT-1" }
                section("Experiment") { accNo = "EXP-1" }
            }
        }
        val experiment = submission.getSectionByType("Experiment")

        assertThat(experiment).isNotNull
        assertThat(experiment.accNo).isEqualTo("EXP-1")
    }

    private fun assertExtendedAttribute(submission: Submission, name: SubFields, value: String) {
        assertThat(submission.attributes).hasSize(1)
        assertThat(submission.attributes.first()).isEqualTo(Attribute(name, value))
    }

    @Test
    fun `get library file sections`() {
        val submission = submission("ABC-123") {
            section("Study") {
                libraryFileName = "LibFile1.tsv"

                section("Data") {
                    accNo = "DT-1"
                }

                section("Experiment") {
                    accNo = "EXP-1"
                    libraryFileName = "LibFile2.tsv"
                }
            }
        }

        val libFileSections = submission.fileListSections()

        assertThat(libFileSections).hasSize(2)
        assertThat(libFileSections.first().libraryFileName).isEqualTo("LibFile2.tsv")
        assertThat(libFileSections.second().libraryFileName).isEqualTo("LibFile1.tsv")
    }
}
