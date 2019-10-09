package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.invalidLinkUrl
import ac.uk.ebi.biostd.itest.factory.simpleSubmissionTsv
import ac.uk.ebi.biostd.persistence.common.SubmissionTypes.Study
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Tag
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagsRefRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.components.IGroupService
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
import org.springframework.web.client.HttpClientErrorException

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired val submissionRepository: SubmissionRepository,
        @Autowired val tagsDataRepository: TagsDataRepository,
        @Autowired val tagsRefRepository: TagsRefRepository,
        @Autowired val groupService: IGroupService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)
            tagsDataRepository.save(AccessTag(name = "Public"))
            tagsRefRepository.save(Tag(classifier = "classifier", name = "tag"))
        }

        @Test
        fun `submit simple submission`() {
            val submission = submission("SimpleAcc1") {
                title = "Simple Submission"
            }

            val response = webClient.submitSingle(submission, SubmissionFormat.XML)

            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val savedSubmission = submissionRepository.getByAccNo("SimpleAcc1")
            assertThat(savedSubmission).isNotNull
            assertThat(savedSubmission).isEqualTo(submission)
        }

        @Test
        fun `submission with tags`() {
            val submission = submission("SimpleAcc2") {
                title = "Simple Submission With Tags"
                tags = mutableListOf(Pair("classifier", "tag"))
            }

            val response = webClient.submitSingle(submission, SubmissionFormat.JSON)

            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val savedSubmission = submissionRepository.getByAccNo("SimpleAcc2")
            assertThat(savedSubmission).isNotNull
            assertThat(savedSubmission).isEqualTo(submission)
        }

        @Test
        fun `submission with rootPath file`() {
            tempFolder.createDirectory("RootPathFolder")
            webClient.uploadFiles(listOf(tempFolder.createFile("RootPathFolder/DataFile5.txt")), "RootPathFolder")

            val submission = submission("S-12364") {
                rootPath = "RootPathFolder"
                title = "Sample Submission"
                section(Study.value) { file("DataFile5.txt") }
            }

            val response = webClient.submitSingle(submission, SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun `submission with group file`() {
            val groupName = "The-Group"
            groupService.addUserInGroup(groupService.createGroup(groupName, "group-desc").name, SuperUser.email)
            webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile1.txt")))
            webClient.uploadGroupFiles(groupName, listOf(tempFolder.createFile("GroupFile2.txt")), "folder")

            val submission = submission("S-54896") {
                title = "Sample Submission"
                section(Study.value) {
                    file("Groups/$groupName/GroupFile1.txt")
                    file("Groups/$groupName/folder/GroupFile2.txt")
                }
            }

            val response = webClient.submitSingle(submission, SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun `resubmit existing submission`() {
            val accNo = "S-ABC123"
            val title = "Simple Submission"
            val submission = simpleSubmissionTsv().toString()
            val response = webClient.submitSingle(submission, SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertExtSubmission(accNo, title)

            val resubmitResponse = webClient.submitSingle(submission, SubmissionFormat.TSV)
            assertThat(resubmitResponse).isNotNull
            assertThat(resubmitResponse.statusCode).isEqualTo(HttpStatus.OK)
            assertExtSubmission(accNo, title, 2)
        }

        @Test
        fun `new submission with past release date`() {
            val pageTab = tsv {
                line("Submission", "S-RLSD123")
                line("Title", "Test Public Submission")
                line("ReleaseDate", "2000-01-31")
                line()
            }.toString()

            val response = webClient.submitSingle(pageTab, SubmissionFormat.TSV)
            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val submission = submissionRepository.getExtendedByAccNo("S-RLSD123")
            assertThat(submission.accNo).isEqualTo("S-RLSD123")
            assertThat(submission.title).isEqualTo("Test Public Submission")
            assertThat(submission.released).isTrue()
            assertThat(submission.accessTags).hasSize(1)
            assertThat(submission.accessTags.first()).isEqualTo("Public")
        }

        @Test
        fun `submit with invalid link Url`() {
            val exception = assertThrows(HttpClientErrorException::class.java) {
                webClient.submitSingle(invalidLinkUrl().toString(), SubmissionFormat.TSV)
            }

            assertThat(exception.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        }

        private fun assertExtSubmission(accNo: String, expectedTitle: String, expectedVersion: Int = 1) {
            val submission = submissionRepository.getExtendedByAccNo(accNo)

            assertThat(submission.title).isEqualTo(expectedTitle)
            assertThat(submission.version).isEqualTo(expectedVersion)
        }
    }
}
