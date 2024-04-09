package ebi.ac.uk.model.extensions

import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import ebi.ac.uk.util.date.asIsoTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

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

        assertThat(submission.releaseDate).isEqualTo("2015-02-20")
        assertExtendedAttribute(submission, SubFields.RELEASE_DATE, "2015-02-20")
    }

    @Test
    fun `release date null`() {
        val submission = submission("ABC-123") {}

        submission.attributes = listOf(Attribute(SubFields.RELEASE_DATE.value, "2015-02-20"))
        submission.releaseDate = null
        assertThat(submission.attributes).hasSize(0)
    }

    @Test
    fun `release time`() {
        val submission = submission("ABC-123") {}
        val dateTime = OffsetDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)
        submission.releaseTime = dateTime

        assertThat(submission.releaseTime).isEqualTo(dateTime)
        assertExtendedAttribute(submission, SubFields.RELEASE_TIME, dateTime.asIsoTime())
    }

    @Test
    fun `creation time`() {
        val submission = submission("ABC-123") {}
        val dateTime = OffsetDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)
        submission.creationTime = dateTime

        assertThat(submission.creationTime).isEqualTo(dateTime)
        assertExtendedAttribute(submission, SubFields.CREATION_TIME, dateTime.asIsoTime())
    }

    @Test
    fun `modification time`() {
        val submission = submission("ABC-123") {}
        val dateTime = OffsetDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)
        submission.modificationTime = dateTime

        assertThat(submission.modificationTime).isEqualTo(dateTime)
        assertExtendedAttribute(submission, SubFields.MODIFICATION_TIME, dateTime.asIsoTime())
    }

    @Test
    fun `secret key`() {
        val submission = submission("ABC-123") {}
        submission.secretKey = "secretKey"

        assertThat(submission.secretKey).isEqualTo("secretKey")
        assertExtendedAttribute(submission, SubFields.SECRET, "secretKey")
    }

    @Test
    fun title() {
        val submission = submission("ABC-123") {}
        submission.title = "Title"

        assertThat(submission.title).isEqualTo("Title")
        assertExtendedAttribute(submission, SubFields.TITLE, "Title")
    }

    @Test
    fun doi() {
        val submission = submission("ABC-123") {}
        submission.doi = "10.6019/ABC-123"

        assertThat(submission.doi).isEqualTo("10.6019/ABC-123")
        assertExtendedAttribute(submission, SubFields.DOI, "10.6019/ABC-123")
    }

    @Test
    fun `root path`() {
        val submission = submission("ABC-123") {}
        submission.rootPath = "/some/path"

        assertThat(submission.rootPath).isEqualTo("/some/path")
        assertExtendedAttribute(submission, SubFields.ROOT_PATH, "/some/path")
    }

    @Test
    fun `accNo template`() {
        val submission = submission("ABC-123") {}
        submission.accNoTemplate = "!{ABC}"

        assertThat(submission.accNoTemplate).isEqualTo("!{ABC}")
        assertExtendedAttribute(submission, SubFields.ACC_NO_TEMPLATE, "!{ABC}")
    }

    @Test
    fun `is collection`() {
        val submission =
            submission("BioImages") {
                section("Project") {}
            }

        assertThat(submission.isCollection).isTrue()
    }

    @Test
    fun `all submission files`() {
        val submission =
            submission("ABC-123") {
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
        assertThat(allFiles.first()).isEqualTo(BioFile("File1.txt"))
        assertThat(allFiles.second()).isEqualTo(BioFile("DataFile1.csv"))
    }

    @Test
    fun `get section by type`() {
        val submission =
            submission("ABC-123") {
                section("Study") {
                    section("Data") { accNo = "DT-1" }
                    section("Experiment") { accNo = "EXP-1" }
                }
            }
        val experiment = submission.getSectionByType("Experiment")

        assertThat(experiment).isNotNull
        assertThat(experiment.accNo).isEqualTo("EXP-1")
    }

    private fun assertExtendedAttribute(
        submission: Submission,
        name: SubFields,
        value: String,
    ) {
        assertThat(submission.attributes).hasSize(1)
        assertThat(submission.attributes.first()).isEqualTo(Attribute(name.value, value))
    }

    @Test
    fun `get file list sections`() {
        val submission =
            submission("ABC-123") {
                section("Study") {
                    fileListName = "FileList1.tsv"

                    section("Data") {
                        accNo = "DT-1"
                    }

                    section("Experiment") {
                        accNo = "EXP-1"
                        fileListName = "FileList2.tsv"
                    }
                }
            }

        val libFileSections = submission.fileListSections()

        assertThat(libFileSections).hasSize(2)
        assertThat(libFileSections.first().fileListName).isEqualTo("FileList2.tsv")
        assertThat(libFileSections.second().fileListName).isEqualTo("FileList1.tsv")
    }

    @Test
    fun `get all sections`() {
        val submission =
            submission("ABC-123") {
                section("Study") {
                    fileListName = "FileList1.tsv"

                    section("Data") {
                        accNo = "DT-1"
                    }

                    section("Experiment") {
                        accNo = "EXP-1"
                        fileListName = "FileList2.tsv"
                    }
                }
            }

        val sections = submission.allSections()

        assertThat(sections).hasSize(3)
        assertThat(sections.first().type).isEqualTo("Study")
        assertThat(sections.second().type).isEqualTo("Data")
        assertThat(sections.third().type).isEqualTo("Experiment")
    }
}
