package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode.NFS
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.ExistingUser
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation.EMAIL_UPDATE
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation.TRANSFER
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.TransferLogDataService
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.SubmissionTransferOptions
import ebi.ac.uk.util.date.toStringDate
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
import java.time.OffsetDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferSubmissionsTest(
    @param:Autowired private val securityTestService: SecurityTestService,
    @param:Autowired private val subRepository: SubmissionPersistenceQueryService,
    @param:Autowired private val transferLogDataService: TransferLogDataService,
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
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                }.toString()

            val accNo2 = "S-CHOWN2"
            val sub2 =
                tsv {
                    line("Submission", accNo2)
                    line("Title", "Change Owner 2")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
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
            assertTransferLog(SuperUser.email, RegularUser.email, ExistingUser.email, TRANSFER)
        }

    @Test
    fun `33-2 superuser transfers specific submissions`() =
        runTest {
            val accNo1 = "S-CHOWN3"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 3")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                }.toString()

            val accNo2 = "S-CHOWN4"
            val sub2 =
                tsv {
                    line("Submission", accNo2)
                    line("Title", "Change Owner 4")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
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
            assertTransferLog(SuperUser.email, RegularUser.email, ExistingUser.email, TRANSFER)
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
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()

                    line("Project")
                }.toString()
            assertThat(superUserWebClient.submit(collection, TSV)).isSuccessful()
            superUserWebClient.grantPermission(ExistingUser.email, collectionAccNo, ADMIN.name)

            val submission =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Change Owner 5")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line("AttachTo", collectionAccNo)
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)

            assertThat(onBehalfClient.submit(submission, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo).owner).isEqualTo(RegularUser.email)

            val options =
                SubmissionTransferOptions(
                    owner = RegularUser.email,
                    newOwner = ExistingUser.email,
                    accNoList = listOf(accNo),
                )
            adminUserWebClient.transferSubmissions(options)
            assertThat(subRepository.getExtByAccNo(accNo).owner).isEqualTo(ExistingUser.email)
            assertTransferLog(ExistingUser.email, RegularUser.email, ExistingUser.email, TRANSFER)
        }

    @Test
    fun `33-4 regular user transfer submissions`() =
        runTest {
            val accNo = "S-CHOWN6"
            val submission =
                tsv {
                    line("Submission", accNo)
                    line("Title", "Change Owner 6")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
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
    fun `33-5 collection admin cannot transfer all submissions`() =
        runTest {
            val collectionAccNo = "S-CHOWN-ALL-TEST"
            val allowedAccNo = "S-CHOWN-ALL-ALLOWED"
            val deniedAccNo = "S-CHOWN-ALL-DENIED"
            val collection =
                tsv {
                    line("Submission", collectionAccNo)
                    line("AccNoTemplate", "!{$collectionAccNo}")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                    line("Project")
                }.toString()
            assertThat(superUserWebClient.submit(collection, TSV)).isSuccessful()
            superUserWebClient.grantPermission(ExistingUser.email, collectionAccNo, ADMIN.name)

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, RegularUser.email)
            assertThat(
                onBehalfClient.submit(
                    tsv {
                        line("Submission", allowedAccNo)
                        line("Title", "Allowed transfer")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line("AttachTo", collectionAccNo)
                        line()
                    }.toString(),
                    TSV,
                ),
            ).isSuccessful()
            assertThat(
                onBehalfClient.submit(
                    tsv {
                        line("Submission", deniedAccNo)
                        line("Title", "Denied transfer")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()
                    }.toString(),
                    TSV,
                ),
            ).isSuccessful()

            val options =
                SubmissionTransferOptions(
                    owner = RegularUser.email,
                    newOwner = SuperUser.email,
                    accNoList = listOf(allowedAccNo, deniedAccNo),
                )
            val error = assertThrows<WebClientException> { adminUserWebClient.transferSubmissions(options) }

            assertThat(error.message).contains("The user '${ExistingUser.email}' is not allowed to perform this action")
            assertThat(subRepository.getExtByAccNo(allowedAccNo).owner).isEqualTo(RegularUser.email)
            assertThat(subRepository.getExtByAccNo(deniedAccNo).owner).isEqualTo(RegularUser.email)
        }

    @Test
    fun `33-6 superuser transfers submissions to non existing user`() =
        runTest {
            val accNo1 = "S-CHOWN7"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 7")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
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
            assertTransferLog(SuperUser.email, RegularUser.email, "new_user@ebi.ac.uk", TRANSFER)
        }

    @Test
    fun `33-7 superuser transfers submissions to non existing user without name`() =
        runTest {
            val accNo1 = "S-CHOWN8"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 8")
                    line("ReleaseDate", "2099-09-21")
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
                    newOwner = "new_non_existing_user@ebi.ac.uk",
                )
            val error = assertThrows<WebClientException> { superUserWebClient.transferSubmissions(options) }
            assertThat(error.message).contains("User name required for new owner")
        }

    @Test
    fun `33-8 superuser transfers submissions with email update`() =
        runTest {
            securityTestService.ensureUserRegistration(EmailUpdateUser)
            val accNo1 = "S-CHOWN9"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 9")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, EmailUpdateUser.email)

            assertThat(onBehalfClient.submit(sub1, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(EmailUpdateUser.email)

            val options =
                SubmissionTransferOptions(
                    owner = EmailUpdateUser.email,
                    newOwner = "new_email@ebi.ac.uk",
                )
            superUserWebClient.transferEmailUpdate(options)
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo("new_email@ebi.ac.uk")
            assertTransferLog(SuperUser.email, EmailUpdateUser.email, "new_email@ebi.ac.uk", EMAIL_UPDATE)
        }

    @Test
    fun `33-9 superuser transfers submissions with email update to existing`() =
        runTest {
            securityTestService.ensureUserRegistration(EmailUpdateExistingUser)
            val accNo1 = "S-CHOWN10"
            val sub1 =
                tsv {
                    line("Submission", accNo1)
                    line("Title", "Change Owner 10")
                    line("ReleaseDate", OffsetDateTime.now().toStringDate())
                    line()
                }.toString()

            val onBehalfClient =
                SecurityWebClient
                    .create("http://localhost:$serverPort")
                    .getAuthenticatedClient(SuperUser.email, SuperUser.password, EmailUpdateExistingUser.email)

            assertThat(onBehalfClient.submit(sub1, TSV)).isSuccessful()
            assertThat(subRepository.getExtByAccNo(accNo1).owner).isEqualTo(EmailUpdateExistingUser.email)

            val options =
                SubmissionTransferOptions(
                    owner = EmailUpdateExistingUser.email,
                    newOwner = ExistingUser.email,
                )
            val error = assertThrows<WebClientException> { superUserWebClient.transferEmailUpdate(options) }
            assertThat(error.message)
                .contains("There is a user already registered with the email address '${ExistingUser.email}'.")
        }

    private fun assertTransferLog(
        user: String,
        sourceEmail: String,
        targetEmail: String,
        operation: TransferOperation,
    ) {
        val transferLog =
            transferLogDataService.findLatest(
                sourceEmail,
                targetEmail,
                operation,
            )
        assertThat(transferLog).isNotNull
        assertThat(transferLog!!.user).isEqualTo(user)
    }

    private object EmailUpdateUser : TestUser {
        override val username = "Email Update User"
        override val email = "email-update@ebi.ac.uk"
        override val password = "678910"
        override val superUser = false
        override val storageMode = NFS
    }

    private object EmailUpdateExistingUser : TestUser {
        override val username = "Email Update Existing User"
        override val email = "email-update-existing@ebi.ac.uk"
        override val password = "678910"
        override val superUser = false
        override val storageMode = NFS
    }
}
