package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig

@ExtendWith(MockKExtension::class)
class SecurityServiceTest(
    @MockK private val bioWebClient: BioWebClient,
) {
    private val testInstance = SecurityService()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockkObject(SecurityWebClient)
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient
    }

    @Test
    fun `grant permission`() =
        runTest {
            coEvery { bioWebClient.grantPermission(USER, ACC_NO, ACCESS_TYPE) } answers { nothing }

            testInstance.grantPermission(PermissionRequest(securityConfig, ACCESS_TYPE, USER, ACC_NO))

            coVerify(exactly = 1) { bioWebClient.grantPermission(USER, ACC_NO, ACCESS_TYPE) }
        }

    private companion object {
        private const val ACC_NO = "BioImages"
        private const val ACCESS_TYPE = "READ"
        private const val PASSWORD = "password"
        private const val SERVER = "server"
        private const val USER = "user"

        private val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
    }
}
