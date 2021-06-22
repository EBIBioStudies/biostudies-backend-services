package ac.uk.ebi.biostd.itest.test.security

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.RegularUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.AccessPermissionRepository
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
internal class PermissionApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {

    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(PersistenceConfig::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class GivePermissionToUser(
        @Autowired private val userDataRepository: UserDataRepository,
        @Autowired private val accessTagRepository: AccessTagDataRepo,
        @Autowired private val accessPermissionRepository: AccessPermissionRepository,
        @Autowired private val securityTestService: SecurityTestService

    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var superWebClient: BioWebClient
        private lateinit var regularWebClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            securityTestService.registerUser(RegularUser)

            superWebClient = getWebClient(serverPort, SuperUser)
            regularWebClient = getWebClient(serverPort, RegularUser)

            userDataRepository.save(dbUser)
            accessTagRepository.save(dbAccessTag)
        }

        @Test
        fun `give permission to a user by superUser`() {
            accessPermissionRepository.deleteAll()

            superWebClient.givePermissionToUser(dbUser.email, dbAccessTag.name, "READ")

            val permissions = accessPermissionRepository.findAll()

            assertThat(permissions).hasSize(1)
            assertThat(permissions.first().user.email).isEqualTo(dbUser.email)
            assertThat(permissions.first().accessTag.name).isEqualTo(dbAccessTag.name)
        }

        @Test
        fun `give permission to a user by regularUser`() {
            accessPermissionRepository.deleteAll()

            assertThatExceptionOfType(WebClientException::class.java).isThrownBy {
                regularWebClient.givePermissionToUser(dbUser.email, dbAccessTag.name, "READ")
            }
        }
    }

    private companion object {
        val dbUser = DbUser(
            id = 1,
            email = "test@email.com",
            fullName = "fullName",
            secret = "secret",
            keyTime = 2,
            passwordDigest = ByteArray(1)
        )
        val dbAccessTag = DbAccessTag(id = 2, name = "accessTagName")
    }
}
