package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.model.extensions.title
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionOnBehalfTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val userDataRepository: UserDataRepository,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(RegularUser)
            securityTestService.ensureSequence("S-BSST")
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `14-1 submission on behalf another user`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "ON-BEHALF-001")
                    line("Title", "Submission Title")
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            val response = onBehalfClient.submit(submission, TSV)
            assertThat(response).isSuccessful()

            val accNo = response.body.accNo
            assertThat(toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo(accNo))).isEqualTo(
                submission(accNo) {
                    title = "Submission Title"
                },
            )
        }

    @Test
    fun `14-2 submission on behalf new user`() =
        runTest {
            val username = "Jhon doe"
            val email = "jhon@doe.email.com"

            val submission =
                tsv {
                    line("Submission", "ON-BEHALF-002")
                    line("Title", "Submission Title")
                }.toString()

            val response =
                webClient.submit(
                    submission,
                    TSV,
                    SubmitParameters(storageMode = storageMode),
                    OnBehalfParameters(email, username),
                )
            val saved = submissionRepository.getExtByAccNo(response.body.accNo)

            assertThat(saved.owner).isEqualTo(email)
            assertThat(saved.submitter).isEqualTo(SuperUser.email)
            val newUser = userDataRepository.findByEmail(email)
            assertThat(newUser).isNotNull()
            assertThat(newUser!!.active).isFalse()
            assertThat(newUser.notificationsEnabled).isFalse()
        }

    @Test
    fun `14-3 submission on behalf created user with files in his folder`() =
        runTest {
            val regularClient = getWebClient(serverPort, RegularUser)

            regularClient.uploadFile(tempFolder.createFile("ownerFile.txt"))
            webClient.uploadFile(tempFolder.createFile("submitterFile.txt"))

            val submission =
                tsv {
                    line("Submission", "ON-BEHALF-003")
                    line("Title", "Submission Title")
                    line()

                    line("Study")
                    line()

                    line("File", "ownerFile.txt")
                    line()

                    line("File", "submitterFile.txt")
                    line()
                }.toString()

            val onBehalfClient = getWebClient(serverPort, SuperUser, onBehalf = RegularUser)
            val response = onBehalfClient.submit(submission, TSV)
            assertThat(response).isSuccessful()

            val subRelPath = submissionRepository.findExtByAccNo(response.body.accNo)?.relPath
            val filesFolder = submissionPath.resolve("$subRelPath/Files")
            assertThat(filesFolder.resolve("ownerFile.txt")).exists()
            assertThat(filesFolder.resolve("submitterFile.txt")).exists()
        }

    @Test
    fun `14-4 submission on behalf when owner and submitter has the same file`() =
        runTest {
            val regularClient = getWebClient(serverPort, RegularUser)

            regularClient.uploadFile(tempFolder.createDirectory("a").createNewFile("file.txt", "owner data"))
            webClient.uploadFile(tempFolder.createDirectory("b").createNewFile("file.txt", "submitter data"))

            val submission =
                tsv {
                    line("Submission", "ON-BEHALF-004")
                    line("Title", "Submission Title")
                    line()

                    line("Study")
                    line()

                    line("File", "file.txt")
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            val response = onBehalfClient.submit(submission, TSV)
            assertThat(response).isSuccessful()

            val subRelPath = submissionRepository.findExtByAccNo(response.body.accNo)?.relPath
            val testFile = submissionPath.resolve("$subRelPath/Files/file.txt")
            assertThat(testFile).exists()
            assertThat(testFile).hasContent("submitter data")
        }

    @Test
    fun `14-5 On behalf with manager with another user submission update user Owner`() =
        runTest {
            securityTestService.ensureUserRegistration(AnohterUser)
            val submission =
                tsv {
                    line("Submission")
                    line("Title", "Submission Title")
                }.toString()

            val anotherUserClient = getWebClient(serverPort, AnohterUser)
            val response = anotherUserClient.submit(submission, TSV)
            assertThat(response).isSuccessful()

            val accNo = response.body.accNo
            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            val submissionUpdated =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Submission Title Updated")
                }.toString()

            val updated = onBehalfClient.submit(submissionUpdated, TSV)
            assertThat(updated).isSuccessful()

            val result = submissionRepository.getExtByAccNo(accNo)
            assertThat(result.title).isEqualTo("Submission Title Updated")
            assertThat(result.owner).isEqualTo(RegularUser.email)
            assertThat(result.submitter).isEqualTo(SuperUser.email)
        }

    object AnohterUser : TestUser {
        override val username = "Another User"
        override val email = "another@ebi.ac.uk"
        override val password = "678910"
        override val superUser = false

        override fun asRegisterRequest() = RegisterRequest(username, email, password, notificationsEnabled = true)
    }
}
