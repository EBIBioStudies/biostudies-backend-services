package ac.uk.ebi.biostd.itest.test.user

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThrows
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

private const val GROUP_NAME = "Bio-test-group"
private const val GROUP_DESC = "Bio-test-group description"

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserGroupsApiTest(
    @Autowired private val securityTestService: SecurityTestService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var superWebClient: BioWebClient
    private lateinit var regularWebClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            securityTestService.ensureUserRegistration(RegularUser)
            superWebClient = getWebClient(serverPort, SuperUser)
            regularWebClient = getWebClient(serverPort, RegularUser)
            superWebClient.addUserInGroup(superWebClient.createGroup(GROUP_NAME, GROUP_DESC).name, SuperUser.email)
        }

    @Test
    fun `24-1 get user groups`() =
        runTest {
            val groups = superWebClient.getGroups()

            assertThat(groups).hasSize(1)

            val group = groups.first()
            assertThat(group.description).isEqualTo(GROUP_DESC)
        }

    @Test
    fun `24-2 trying to add a user to unexisting group`() =
        runTest {
            val exception =
                assertThrows<WebClientException> {
                    superWebClient.addUserInGroup(NON_EXISTING_GROUP, SuperUser.email)
                }
            assertThat(exception).hasMessageContaining("The group $NON_EXISTING_GROUP does not exists")
        }

    @Test
    fun `24-3 trying to add a user that does not exist`() =
        runTest {
            val exception =
                assertThrows<WebClientException> {
                    superWebClient.addUserInGroup(GROUP_NAME, NON_EXISTING_USER)
                }
            assertThat(exception).hasMessageContaining("The user $NON_EXISTING_USER does not exists")
        }

    @Test
    fun `24-4 trying to add a user by regularUser`() =
        runTest {
            val exception =
                assertThrows<WebClientException> {
                    regularWebClient.addUserInGroup(GROUP_NAME, NON_EXISTING_USER)
                }

            assertThat(exception).hasMessageContaining("Access is denied")
        }

    companion object {
        const val NON_EXISTING_GROUP = "fakeGroup"
        const val NON_EXISTING_USER = "fakeEmail"
    }

    object SuperUser : TestUser {
        override val username = "Super User Group Test"
        override val email = "gr-test-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
        override val storageMode = StorageMode.NFS
    }

    object RegularUser : TestUser {
        override val username = "Regular User Group Test"
        override val email = "regular-group-test-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false
        override val storageMode = StorageMode.NFS
    }
}
