package ac.uk.ebi.biostd.itest.test.collection

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ebi.ac.uk.asserts.assertThat
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
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ListCollectionsTest(
    @LocalServerPort val serverPort: Int,
    @Autowired val securityTestService: SecurityTestService,
) {
    private lateinit var superUserWebClient: BioWebClient
    private lateinit var regularUserWebClient: BioWebClient
    private lateinit var defaultUserWebClient: BioWebClient
    private lateinit var collectionAdminUserWebClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            setUpUsers()
            registerCollections()
            setUpPermissions()
        }

    @Test
    fun `20-1 list collections for super user`() =
        runTest {
            val collections = superUserWebClient.getCollections()

            assertThat(collections).hasSizeGreaterThanOrEqualTo(2)
            assertThat(collections).anyMatch { it.accno == "SampleCollection" }
            assertThat(collections).anyMatch { it.accno == "DefaultCollection" }
        }

    @Test
    fun `20-2 list collections for regular user`() =
        runTest {
            val collections = regularUserWebClient.getCollections()

            assertThat(collections).hasSizeGreaterThanOrEqualTo(2)
            assertThat(collections).anyMatch { it.accno == "SampleCollection" }
            assertThat(collections).anyMatch { it.accno == "DefaultCollection" }
        }

    @Test
    fun `20-3 list collections for default user`() =
        runTest {
            val collections = defaultUserWebClient.getCollections()

            assertThat(collections).hasSize(1)
            assertThat(collections.first().accno).isEqualTo("DefaultCollection")
        }

    @Test
    fun `20-4 list collections for collection admin user`() =
        runTest {
            val collections = collectionAdminUserWebClient.getCollections()

            assertThat(collections).hasSizeGreaterThanOrEqualTo(2)
            assertThat(collections).anyMatch { it.accno == "SampleCollection" }
            assertThat(collections).anyMatch { it.accno == "DefaultCollection" }
        }

    private suspend fun registerCollections() {
        val sampleCollection =
            tsv {
                line("Submission", "SampleCollection")
                line("AccNoTemplate", "!{S-SAMP}")
                line()

                line("Project")
            }.toString()

        val defaultCollection =
            tsv {
                line("Submission", "DefaultCollection")
                line("AccNoTemplate", "!{S-DFLT}")
                line()

                line("Project")
            }.toString()

        assertThat(superUserWebClient.submit(sampleCollection, TSV)).isSuccessful()
        assertThat(superUserWebClient.submit(defaultCollection, TSV)).isSuccessful()
    }

    private suspend fun setUpUsers() {
        securityTestService.ensureUserRegistration(SuperUser)
        securityTestService.ensureUserRegistration(RegularUser)
        securityTestService.ensureUserRegistration(DefaultUser)
        securityTestService.ensureUserRegistration(CollectionUser)

        superUserWebClient = getWebClient(serverPort, SuperUser)
        regularUserWebClient = getWebClient(serverPort, RegularUser)
        defaultUserWebClient = getWebClient(serverPort, DefaultUser)
        collectionAdminUserWebClient = getWebClient(serverPort, CollectionUser)
    }

    private fun setUpPermissions() {
        superUserWebClient.grantPermission(DefaultUser.email, "DefaultCollection", ATTACH.name)
        superUserWebClient.grantPermission(RegularUser.email, "SampleCollection", ATTACH.name)
        superUserWebClient.grantPermission(CollectionUser.email, "SampleCollection", ADMIN.name)
    }

    object CollectionUser : TestUser {
        override val username = "Collection Admin"
        override val email = "collection-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false
    }

    object RegularUser : TestUser {
        override val username = "Regular Collection User"
        override val email = "regular-collection-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false
    }
}
