package ac.uk.ebi.biostd.itest.test.user

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.clean
import ac.uk.ebi.biostd.itest.common.getWebClient
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ebi.ac.uk.test.clean
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

private const val GROUP_NAME = "Bio-test-group"
private const val GROUP_DESC = "Bio-test-group description"

@Import(PersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class UserGroupsApiTest(
    @Autowired private val securityTestService: SecurityTestService,
    @Autowired val sequenceRepository: SequenceDataRepository,
    @Autowired private val accessPermissionRepository: AccessPermissionRepository,
    @Autowired private val tagsDataRepository: AccessTagDataRepo,
    @LocalServerPort val serverPort: Int
) {
    private lateinit var superWebClient: BioWebClient
    private lateinit var regularWebClient: BioWebClient

    @BeforeAll
    fun init() {
        tempFolder.clean()

        sequenceRepository.deleteAll()
        accessPermissionRepository.deleteAll()
        tagsDataRepository.deleteAll()
        securityTestService.deleteAllDbUsers()

        securityTestService.registerUser(SuperUser)
        securityTestService.registerUser(RegularUser)
        superWebClient = getWebClient(serverPort, SuperUser)
        regularWebClient = getWebClient(serverPort, RegularUser)

        superWebClient.addUserInGroup(superWebClient.createGroup(GROUP_NAME, GROUP_DESC).name, SuperUser.email)
    }

    @BeforeEach
    fun beforeEach() {
        tempFolder.clean()
    }

    @Test
    fun `get user groups`() {
        val groups = superWebClient.getGroups()

        assertThat(groups).hasSize(1)
        assertThat(groups.first()).satisfies {
            assertThat(it.description).isEqualTo(GROUP_DESC)
            assertThat(it.name).isEqualTo(GROUP_NAME)
        }
    }

    @Test
    fun `trying to add a user to unexisting group`() {
        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            superWebClient.addUserInGroup(
                nonExistentGroupName,
                SuperUser.email
            )
        }.withMessageContaining("The group $nonExistentGroupName does not exists")
    }

    @Test
    fun `trying to add a user that does not exist`() {
        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            superWebClient.addUserInGroup(
                GROUP_NAME,
                nonExistentUser
            )
        }.withMessageContaining("The user $nonExistentUser does not exists")
    }

    @Test
    fun `trying to add a user by regularUser`() {
        assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
            regularWebClient.addUserInGroup(
                GROUP_NAME,
                nonExistentUser
            )
        }.withMessageContaining("Access is denied")
    }

    companion object {
        const val nonExistentGroupName = "fakeGroup"
        const val nonExistentUser = "fakeEmail"
    }
}
