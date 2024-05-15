package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.MissingParameter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.PermissionRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SecurityService

@ExtendWith(MockKExtension::class)
internal class GrantCollectionPermissionCommandTest(
    @MockK private val securityService: SecurityService,
) {
    private val testInstance = GrantCollectionPermissionCommand(securityService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `grant collection permission request`() {
        val securityConfig = SecurityConfig("server", "user", "password")
        val request = PermissionRequest(securityConfig, "ATTACH", "user@mail.org", "BioImages")

        every { securityService.grantCollectionPermission(request) } answers { nothing }

        testInstance.parse(
            listOf(
                "-s", "server",
                "-u", "user",
                "-p", "password",
                "-at", "ATTACH",
                "-tu", "user@mail.org",
                "-acc", "BioImages",
            ),
        )

        verify(exactly = 1) { securityService.grantCollectionPermission(request) }
    }

    @Test
    fun `missing arguments`() {
        val exceptionMessage = assertThrows<MissingParameter> { testInstance.parse(listOf("-u", "manager")) }.message
        assertThat(exceptionMessage).isEqualTo("Missing option \"--server\".")
    }
}
