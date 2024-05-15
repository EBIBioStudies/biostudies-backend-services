package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.verify
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
    fun `grant permission`() {
        every { bioWebClient.grantCollectionPermission(USER, ACCESS_TAG, ACCESS_TYPE) } answers { nothing }

        testInstance.grantCollectionPermission(PermissionRequest(securityConfig, ACCESS_TYPE, USER, ACCESS_TAG))

        verify(exactly = 1) { bioWebClient.grantCollectionPermission(USER, ACCESS_TAG, ACCESS_TYPE) }
    }

    @Test
    fun `create and grant permission`() {
        every { bioWebClient.grantSubmissionPermission(USER, ACCESS_TAG, ACCESS_TYPE) } answers { nothing }

        testInstance.grantSubmissionPermission(PermissionRequest(securityConfig, ACCESS_TYPE, USER, ACCESS_TAG))

        verify(exactly = 1) { bioWebClient.grantSubmissionPermission(USER, ACCESS_TAG, ACCESS_TYPE) }
    }

    private companion object {
        private const val ACCESS_TAG = "BioImages"
        private const val ACCESS_TYPE = "READ"
        private const val PASSWORD = "password"
        private const val SERVER = "server"
        private const val USER = "user"

        private val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
    }
}
