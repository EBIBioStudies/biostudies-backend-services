package uk.ac.ebi.biostd.client.cli.commands

import ebi.ac.uk.extended.model.StorageMode.NFS
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.TransferRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class)
internal class TransferCommandTest(
    @MockK private val submissionService: SubmissionService,
) {
    private val testInstance = TransferCommand(submissionService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `transfer request`() {
        val securityConfig = SecurityConfig("server", "user", "password")
        val request = TransferRequest("S-BSST1", NFS, securityConfig)

        coEvery { submissionService.transfer(request) } answers { nothing }

        testInstance.parse(
            listOf(
                "-s",
                "server",
                "-u",
                "user",
                "-p",
                "password",
                "-ac",
                "S-BSST1",
                "-t",
                "NFS",
            ),
        )

        coVerify(exactly = 1) { submissionService.transfer(request) }
    }

    @Test
    fun `transfer with invalid target`() {
        val exception =
            assertThrows<IllegalStateException> {
                testInstance.parse(
                    listOf(
                        "-s",
                        "server",
                        "-u",
                        "user",
                        "-p",
                        "password",
                        "-ac",
                        "S-BSST1",
                        "-t",
                        "INVALID",
                    ),
                )
            }
        assertThat(exception.message).isEqualTo("Unknown storage mode INVALID")
    }
}
