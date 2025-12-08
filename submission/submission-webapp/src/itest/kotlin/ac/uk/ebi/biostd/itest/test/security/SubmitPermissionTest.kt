package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.ExistingUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.common.model.AccessType.UPDATE
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
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
class SubmitPermissionTest(
    @param:Autowired private val securityTestService: SecurityTestService,
    @param:LocalServerPort val serverPort: Int,
) {
    private val collection =
        tsv {
            line("Submission", "ACollection")
            line("AccNoTemplate", "!{S-APR}")
            line()

            line("Project")
        }.toString()

    private lateinit var superUserWebClient: BioWebClient
    private lateinit var regularUserWebClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(ExistingUser)
            securityTestService.ensureUserRegistration(ImpersonatedUser)

            superUserWebClient = getWebClient(serverPort, SuperUser)
            regularUserWebClient = getWebClient(serverPort, ExistingUser)
        }

    @Test
    fun `4-1 Superuser creates a collection`() =
        runTest {
            assertThat(superUserWebClient.submit(collection, TSV)).isSuccessful()
        }

    @Test
    fun `4-2 Regular user can not create a collection`() =
        runTest {
            assertThrows<WebClientException> {
                regularUserWebClient.submit(collection, TSV)
            }
        }

    @Test
    fun `4-3 Regular user submits a collection submision without attach permission`() =
        runTest {
            val project =
                tsv {
                    line("Submission", "TestCollection")
                    line("AccNoTemplate", "!{S-CLL}")
                    line()

                    line("Project")
                }.toString()

            val submission =
                tsv {
                    line("Submission")
                    line("AttachTo", "TestCollection")
                    line("Title", "Test Submission")
                }.toString()

            assertThat(superUserWebClient.submit(project, TSV)).isSuccessful()
            val exception =
                assertThrows<WebClientException> {
                    regularUserWebClient.submit(submission, TSV)
                }
            assertThat(
                exception,
            ).hasMessageContaining("The user register_user@ebi.ac.uk is not allowed to submit to TestCollection collection")
        }

    @Test
    fun `4-4 Regular user submits a collection submision attach permission`() =
        runTest {
            val project =
                tsv {
                    line("Submission", "TestCollection2")
                    line("AccNoTemplate", "!{S-COLL}")
                    line()

                    line("Project")
                }.toString()

            val submission =
                tsv {
                    line("Submission")
                    line("AttachTo", "TestCollection2")
                    line("Title", "Test Submission")
                }.toString()

            assertThat(superUserWebClient.submit(project, TSV)).isSuccessful()
            superUserWebClient.grantPermission(ExistingUser.email, "TestCollection2", ATTACH.name)

            assertThat(regularUserWebClient.submit(submission, TSV)).isSuccessful()
        }

    @Test
    fun `4-5 Regular user register and submits to default project`() =
        runTest {
            val project =
                tsv {
                    line("Submission", "TestCollection3")
                    line("AccNoTemplate", "!{S-CLC}")
                    line()

                    line("Project")
                }.toString()

            val submission =
                tsv {
                    line("Submission")
                    line("AttachTo", "TestCollection3")
                    line("Title", "Test Submission")
                }.toString()

            create("http://localhost:$serverPort").registerUser(NewUser.asRegisterRequest())
            assertThat(superUserWebClient.submit(project, TSV)).isSuccessful()

            superUserWebClient.grantPermission(NewUser.email, "TestCollection3", ATTACH.name)
            assertThat(getWebClient(serverPort, NewUser).submit(submission, TSV)).isSuccessful()
        }

    @Test
    fun `4-6 Regular user submits with collection admin permission`() =
        runTest {
            val project =
                tsv {
                    line("Submission", "TestCollection4")
                    line("AccNoTemplate", "!{S-CLCT}")
                    line()

                    line("Project")
                }.toString()

            val submission =
                tsv {
                    line("Submission")
                    line("AttachTo", "TestCollection4")
                    line("Title", "Test Submission")
                }.toString()

            assertThat(superUserWebClient.submit(project, TSV)).isSuccessful()
            superUserWebClient.grantPermission(ExistingUser.email, "TestCollection4", ADMIN.name)

            assertThat(regularUserWebClient.submit(submission, TSV)).isSuccessful()
        }

    @Test
    fun `4-7 Regular user resubmits another user submission with collection admin permission`() =
        runTest {
            val project =
                tsv {
                    line("Submission", "TestCollection5")
                    line("AccNoTemplate", "!{S-TCLT}")
                    line()

                    line("Project")
                }.toString()

            val submission =
                tsv {
                    line("Submission")
                    line("AttachTo", "TestCollection5")
                    line("Title", "Test Submission")
                }.toString()

            assertThat(superUserWebClient.submit(project, TSV)).isSuccessful()
            superUserWebClient.grantPermission(ExistingUser.email, "TestCollection5", ADMIN.name)

            assertThat(regularUserWebClient.submit(submission, TSV)).isSuccessful()

            val resubmission =
                tsv {
                    line("Submission", "S-TCLT1")
                    line("AttachTo", "TestCollection5")
                    line("Title", "Test Resubmission")
                }.toString()

            assertThat(regularUserWebClient.submit(resubmission, TSV)).isSuccessful()
        }

    @Test
    fun `4-8 Regular user resubmits its own submission`() =
        runTest {
            val project =
                tsv {
                    line("Submission", "TestCollection6")
                    line("AccNoTemplate", "!{T-CLLC}")
                    line()

                    line("Project")
                }.toString()

            val submission =
                tsv {
                    line("Submission")
                    line("AttachTo", "TestCollection6")
                    line("Title", "Test Submission")
                }.toString()

            val impersonatedUserClient = getWebClient(serverPort, ImpersonatedUser)
            val onBehalfClient = getWebClient(serverPort, SuperUser, ImpersonatedUser)

            assertThat(superUserWebClient.submit(project, TSV)).isSuccessful()
            assertThat(onBehalfClient.submit(submission, TSV)).isSuccessful()

            val resubmission =
                tsv {
                    line("Submission", "T-CLLC1")
                    line("AttachTo", "TestCollection6")
                    line("Title", "Test Resubmission")
                }.toString()
            assertThat(impersonatedUserClient.submit(resubmission, TSV)).isSuccessful()
        }

    @Test
    fun `4-9 Regular user resubmits another user submission`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-SBMT1")
                    line("Title", "Test Submission")
                    line()

                    line("Study")
                    line()
                }.toString()

            assertThat(superUserWebClient.submit(submission, TSV)).isSuccessful()

            val resubmission =
                tsv {
                    line("Submission", "S-SBMT1")
                    line("Title", "Test Resubmission")
                    line()

                    line("Study")
                    line()
                }.toString()

            val exception = assertThrows<WebClientException> { regularUserWebClient.submit(resubmission, TSV) }
            assertThat(exception)
                .hasMessageContaining("The user register_user@ebi.ac.uk is not allowed to update the submission S-SBMT1")
        }

    @Test
    fun `4-10 Regular user resubmits another user submission with UPDATE permission`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-SBMT2")
                    line("Title", "Test Submission")
                    line()

                    line("Study")
                    line()
                }.toString()

            assertThat(superUserWebClient.submit(submission, TSV)).isSuccessful()

            val resubmission =
                tsv {
                    line("Submission", "S-SBMT2")
                    line("Title", "Test Resubmission")
                    line()

                    line("Study")
                    line()
                }.toString()

            superUserWebClient.grantPermission(ExistingUser.email, "S-SBMT2", UPDATE.name)
            assertThat(regularUserWebClient.submit(resubmission, TSV)).isSuccessful()
        }

    object NewUser : TestUser {
        override val username = "New User"
        override val email = "new_user@ebi.ac.uk"
        override val password = "1234"
        override val superUser = false
        override val storageMode = StorageMode.NFS
    }

    object ImpersonatedUser : TestUser {
        override val username = "Impersonated User"
        override val email = "impersonated_user@ebi.ac.uk"
        override val password = "1234"
        override val superUser = false
        override val storageMode = StorageMode.NFS
    }
}
