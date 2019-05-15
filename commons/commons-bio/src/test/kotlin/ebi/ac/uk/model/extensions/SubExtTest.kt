package ebi.ac.uk.model.extensions

import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.errors.InvalidDateFormatException
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

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
        val time = LocalDate.parse("2015-02-20")
        val submission = submission("ABC-123") {}
        submission.releaseDate = time

        assertExtendedAttribute(submission, SubFields.RELEASE_DATE, "2015-02-20")
    }

    @Test
    fun `release date in string format`() {
        val expectedDate = LocalDate.parse("2015-02-20")
        val submission = submission("ABC-123") {
            attribute("ReleaseDate", "2015-02-20")
        }

        assertThat(submission.releaseDate).isEqualTo(expectedDate)
    }

    @Test
    fun `release date with invalid format`() {
        val submission = submission("ABC-123") {
            attribute("ReleaseDate", "2015/02/20")
        }

        val exception = assertThrows<InvalidDateFormatException> { submission.releaseDate }
        assertThat(exception.message).isEqualTo(
            "Invalid date format provided for date 2015/02/20. Expected format is YYYY-MM-DD")
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
                libraryFile = "LibFile1.tsv"

                section("Data") {
                    accNo = "DT-1"
                }

                section("Experiment") {
                    accNo = "EXP-1"
                    libraryFile = "LibFile2.tsv"
                }
            }
        }

        val libFileSections = submission.libFileSections()

        assertThat(libFileSections).hasSize(2)
        assertThat(libFileSections.first().libraryFile).isEqualTo("LibFile2.tsv")
        assertThat(libFileSections.second().libraryFile).isEqualTo("LibFile1.tsv")
    }
}
