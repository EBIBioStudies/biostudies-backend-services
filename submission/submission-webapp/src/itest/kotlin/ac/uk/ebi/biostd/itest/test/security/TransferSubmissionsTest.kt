package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.ExistingUser
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.SubmissionTransferOptions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferSubmissionsTest(
    @param:Autowired private val securityTestService: SecurityTestService,
    @param:Autowired private val subRepository: SubmissionPersistenceQueryService,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var superUserWebClient: BioWebClient
    private lateinit var regularUserWebClient: BioWebClient
    private lateinit var adminUserWebClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(RegularUser)
            securityTestService.ensureUserRegistration(ExistingUser)

            superUserWebClient = getWebClient(serverPort, SuperUser)
            adminUserWebClient = getWebClient(serverPort, ExistingUser)
            regularUserWebClient = getWebClient(serverPort, RegularUser)
        }

    @Test
    fun `33-1 superuser transfers all submissions`() =
        runTest {
            val accNo1 = "S-CHOWN1"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 1")
                    line()
                }.toString()

            val accNo2 = "S-CHOWN2"
            val sub2 =
                tsv {
                    line("Submission", accNo2)
                    line("Title", "Change Owner 2")
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(sub1, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(RegularUser.email)

            assertThat(onBehalfClient.submit(sub2, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo2).owner).isEqualTo(RegularUser.email)

            val options = SubmissionTransferOptions(owner = RegularUser.email, newOwner = ExistingUser.email)
            superUserWebClient.transferSubmissions(options)
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(ExistingUser.email)
            assertThat(subRepository.getExtByAccNo(accNo2).owner).isEqualTo(ExistingUser.email)
        }

    @Test
    fun `33-2 superuser transfers specific submissions`() =
        runTest {
            val accNo1 = "S-CHOWN3"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 3")
                    line()
                }.toString()

            val accNo2 = "S-CHOWN4"
            val sub2 =
                tsv {
                    line("Submission", accNo2)
                    line("Title", "Change Owner 4")
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(sub1, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(RegularUser.email)

            assertThat(onBehalfClient.submit(sub2, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo2).owner).isEqualTo(RegularUser.email)

            val options =
                SubmissionTransferOptions(
                    owner = RegularUser.email,
                    newOwner = ExistingUser.email,
                    accNoList = listOf(accNo1),
                )
            superUserWebClient.transferSubmissions(options)
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(ExistingUser.email)
            assertThat(subRepository.getExtByAccNo(accNo2).owner).isEqualTo(RegularUser.email)
        }

    @Test
    fun `33-3 admin user transfers submissions`() =
        runTest {
            val accNo = "S-CHOWN5"
            val collectionAccNo = "S-CHOWN-TEST"
            val collection =
                tsv {
                    line("Submission", collectionAccNo)
                    line("AccNoTemplate", "!{$collectionAccNo}")
                    line()

                    line("Project")
                }.toString()
            assertThat(superUserWebClient.submit(collection, TSV)).isSuccessful()
            superUserWebClient.grantPermission(ExistingUser.email, collectionAccNo, ADMIN.name)

            val submission =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Change Owner 5")
                    line("AttachTo", collectionAccNo)
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(submission, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo).owner).isEqualTo(RegularUser.email)

            val options = SubmissionTransferOptions(owner = RegularUser.email, newOwner = ExistingUser.email)
            adminUserWebClient.transferSubmissions(options)
            assertThat(subRepository.getExtByAccNo(accNo).owner).isEqualTo(ExistingUser.email)
        }

    @Test
    fun `33-4 regular user transfer submissions`() =
        runTest {
            val accNo = "S-CHOWN6"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Change Owner 6")
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(submission, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo).owner).isEqualTo(RegularUser.email)

            val options = SubmissionTransferOptions(owner = RegularUser.email, newOwner = ExistingUser.email)
            val error = assertThrows<WebClientException> { regularUserWebClient.transferSubmissions(options) }
            assertThat(error.message).contains("The user '${RegularUser.email}' is not allowed to perform this action")
        }

    @Test
    fun `33-5 superuser transfers submissions to non existing user`() =
        runTest {
            val accNo1 = "S-CHOWN7"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 7")
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(sub1, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(RegularUser.email)

            val options =
                SubmissionTransferOptions(
                    owner = RegularUser.email,
                    newOwner = "new_user@ebi.ac.uk",
                    userName = "New User",
                )
            superUserWebClient.transferSubmissions(options)
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo("new_user@ebi.ac.uk")
        }

    @Test
    fun `33-6 superuser transfers submissions to non existing user without name`() =
        runTest {
            val accNo1 = "S-CHOWN8"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 8")
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(sub1, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(RegularUser.email)

            val options =
                SubmissionTransferOptions(
                    owner = RegularUser.email,
                    newOwner = "new_user@ebi.ac.uk",
                )
            val error = assertThrows<WebClientException> { superUserWebClient.transferSubmissions(options) }
            assertThat(error.message).contains("User name required for new owner")
        }

    @Test
    fun `33-7 superuser transfers submissions with email update`() =
        runTest {
            val accNo1 = "S-CHOWN9"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 9")
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(sub1, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(RegularUser.email)

            val options =
                SubmissionTransferOptions(
                    owner = RegularUser.email,
                    newOwner = "new_email@ebi.ac.uk",
                )
            superUserWebClient.transferEmailUpdate(options)
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo("new_email@ebi.ac.uk")
        }

    @Test
    fun `33-8 superuser transfers submissions with email update to existing`() =
        runTest {
            val accNo1 = "S-CHOWN10"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 10")
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(sub1, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(RegularUser.email)

            val options =
                SubmissionTransferOptions(
                    owner = RegularUser.email,
                    newOwner = ExistingUser.email,
                )
            val error = assertThrows<WebClientException> { superUserWebClient.transferEmailUpdate(options) }
            assertThat(error.message)
                .contains("There is a user already registered with the email address '${ExistingUser.email}'.")
        }
}
