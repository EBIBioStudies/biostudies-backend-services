package ac.uk.ebi.biostd.itest.test.user

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
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
    fun `24-1 get user groups`() {
        val groups = superWebClient.getGroups()

        assertThat(groups).hasSize(1)

        val group = groups.first()
        assertThat(group.description).isEqualTo(GROUP_DESC)
    }

    @Test
    fun `24-2 trying to add a user to unexisting group`() {
        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { superWebClient.addUserInGroup(NON_EXISTING_GROUP, SuperUser.email) }
            .withMessageContaining("The group $NON_EXISTING_GROUP does not exists")
    }

    @Test
    fun `24-3 trying to add a user that does not exist`() {
        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { superWebClient.addUserInGroup(GROUP_NAME, NON_EXISTING_USER) }
            .withMessageContaining("The user $NON_EXISTING_USER does not exists")
    }

    @Test
    fun `24-4 trying to add a user by regularUser`() {
        assertThatExceptionOfType(WebClientException::class.java)
            .isThrownBy { regularWebClient.addUserInGroup(GROUP_NAME, NON_EXISTING_USER) }
            .withMessageContaining("Access is denied")
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
    }

    object RegularUser : TestUser {
        override val username = "Regular User Group Test"
        override val email = "regular-group-test-biostudies-mgmt@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false
    }
}
