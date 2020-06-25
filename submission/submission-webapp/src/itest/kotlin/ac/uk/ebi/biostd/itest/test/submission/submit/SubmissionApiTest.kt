package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.api.dto.UserRegistration
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.util.collections.ifRight
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertFailsWith

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmissionApiTest(
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val sequenceRepository: SequenceDataRepository,
        @Autowired val tagsRefRepository: TagDataRepository,
        @Autowired val groupService: IGroupService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)

            sequenceRepository.save(Sequence("S-BSST"))
            tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))
        }

        @Test
        fun `submit with submission object`() {
            val submission = submission("SimpleAcc1") {
                title = "Simple Submission"
            }

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("SimpleAcc1")).isEqualTo(submission("SimpleAcc1") {
                title = "Simple Submission"
            })
        }

        @Test
        fun `empty accNo`() {
            val submission = tsv {
                line("Submission")
                line("Title", "Empty AccNo")
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-BSST0")).isEqualTo(
                submission("S-BSST0") {
                    title = "Empty AccNo"
                }
            )
        }

        @Test
        fun `submission with root path`() {
            val submission = tsv {
                line("Submission", "S-12364")
                line("Title", "Sample Submission")
                line("RootPath", "RootPathFolder")
                line()

                line("Study")
                line()

                line("File", "DataFile5.txt")
                line()
            }.toString()

            tempFolder.createDirectory("RootPathFolder")
            webClient.uploadFiles(listOf(tempFolder.createFile("RootPathFolder/DataFile5.txt")), "RootPathFolder")

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-12364")).isEqualTo(
                submission("S-12364") {
                    title = "Sample Submission"
                    rootPath = "RootPathFolder"
                    section("Study") { file("DataFile5.txt") }
                }
            )
        }

        @Test
        fun `submission with on behalf another user`() {
            createUser(RegularUser, serverPort)

            val submission = tsv {
                line("Submission")
                line("Title", "Submission Title")
            }.toString()

            val onBehalfClient = SecurityWebClient
                .create("http://localhost:$serverPort")
                .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            val response = onBehalfClient.submitSingle(submission, TSV)
            assertThat(response).isSuccessful()

            val accNo = response.body.accNo
            assertThat(submissionRepository.getByAccNo(accNo)).isEqualTo(
                submission(accNo) {
                    title = "Submission Title"
                }
            )
        }

        @Test
        fun `submission with on behalf new user`() {
            val username = "Jhon doe"
            val email = "jhon@doe.email.com"

            val submission = tsv {
                line("Submission")
                line("Title", "Submission Title")
            }.toString()

            val response = webClient.submitSingle(submission, TSV, UserRegistration(username, email))
            val saved = submissionRepository.getExtByAccNo(response.body.accNo)
            assertThat(saved.owner).isEqualTo(email)
            assertThat(saved.submitter).isEqualTo(SuperUser.email)
        }

        @Test
        fun `submission with generic root section`() {
            val submission = tsv {
                line("Submission", "E-MTAB123")
                line("Title", "Generic Submission")
                line()

                line("Experiment")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("E-MTAB123")).isEqualTo(
                submission("E-MTAB123") {
                    title = "Generic Submission"
                    section("Experiment") { }
                }
            )
        }

        @Test
        fun `submission with tags`() {
            val submission = tsv {
                line("Submission", "S-TEST123")
                line("Title", "Submission With Tags")
                line()

                line("Study", "SECT-001", "", "classifier:tag")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-TEST123")).isEqualTo(
                submission("S-TEST123") {
                    title = "Submission With Tags"
                    section("Study") {
                        accNo = "SECT-001"
                        tags = mutableListOf(Pair("Classifier", "Tag"))
                    }
                }
            )
        }

        @Test
        fun `submission with group file`() {
            val groupName = "The-Group"
            val submission = tsv {
                line("Submission", "S-54896")
                line("Title", "Sample Submission")
                line()

                line("Study")
                line()

                line("File", "groups/$groupName/GroupFile1.txt")
                line()

                line("File", "groups/$groupName/folder/GroupFile2.txt")
                line()
            }.toString()

            groupService.addUserInGroup(groupService.createGroup(groupName, "group-desc").name, SuperUser.email)
            webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile1.txt")))
            webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile2.txt")), "folder")

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            assertThat(submissionRepository.getByAccNo("S-54896")).isEqualTo(
                submission("S-54896") {
                    title = "Sample Submission"
                    section("Study") {
                        file("groups/$groupName/GroupFile1.txt")
                        file("groups/$groupName/folder/GroupFile2.txt")
                    }
                }
            )
        }

        @Test
        fun `resubmit existing submission`() {
            val submission = tsv {
                line("Submission", "S-ABC123")
                line("Title", "Simple Submission")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val original = submissionRepository.getExtendedByAccNo("S-ABC123")
            assertThat(original.title).isEqualTo("Simple Submission")
            assertThat(original.version).isEqualTo(1)

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()
            val resubmitted = submissionRepository.getExtendedByAccNo("S-ABC123")
            assertThat(resubmitted.title).isEqualTo("Simple Submission")
            assertThat(resubmitted.version).isEqualTo(2)
        }

        @Test
        fun `new submission with past release date`() {
            val submission = tsv {
                line("Submission", "S-RLSD123")
                line("Title", "Test Public Submission")
                line("ReleaseDate", "2000-01-31")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-RLSD123")
            assertThat(savedSubmission.accNo).isEqualTo("S-RLSD123")
            assertThat(savedSubmission.title).isEqualTo("Test Public Submission")
            assertThat(savedSubmission.released).isTrue()
            assertThat(savedSubmission.accessTags).hasSize(1)
            assertThat(savedSubmission.accessTags.first().name).isEqualTo("Public")
        }

        @Test
        fun `new submission with empty accNo subsection table`() {
            val submission = tsv {
                line("Submission", "S-STBL123")
                line("Title", "Test Section Table")
                line()

                line("Study", "SECT-001")
                line()

                line("Data[SECT-001]", "Title")
                line("", "Group 1")
                line()
            }.toString()

            assertThat(webClient.submitSingle(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-STBL123")
            assertThat(savedSubmission.accNo).isEqualTo("S-STBL123")
            assertThat(savedSubmission.title).isEqualTo("Test Section Table")

            val section = savedSubmission.section
            assertThat(section.accNo).isEqualTo("SECT-001")
            assertThat(section.sections).hasSize(1)
            section.sections.first().ifRight {
                assertThat(it.sections).hasSize(1)

                val subSection = it.sections.first()
                assertThat(subSection.accNo).isEmpty()
                assertThat(subSection.attributes).hasSize(1)
                assertThat(subSection.attributes.first().name).isEqualTo("Title")
                assertThat(subSection.attributes.first().value).isEqualTo("Group 1")
            }
        }

        @Test
        fun `submit with invalid link Url`() {
            val exception = assertThrows(WebClientException::class.java) {
                webClient.submitSingle(invalidLinkUrl().toString(), TSV)
            }

            assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        @Test
        fun `submission with validation error`() {
            val submission = submission("S-400") {
                title = "Submission with invalid file"
                section("Study") { file("invalidfile.txt") }
            }

            val exception = assertFailsWith<WebClientException> {
                webClient.submitSingle(submission, SubmissionFormat.XML)
            }
            assertThat(exception.message!!.contains("Submission contains invalid files invalid file.txt"))
        }
    }
}
