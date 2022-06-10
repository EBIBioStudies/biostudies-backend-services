package uk.ac.ebi.biostd.client.cli.commands

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class)
internal class ValidateFileListCommandTest(
    @MockK private val submissionService: SubmissionService
) {
    private var testInstance = ValidateFileListCommand(submissionService)

    @Test
    fun `validate successful`() {
        every { submissionService.validateFileList(validateRequest) } returns Unit

        testInstance.parse(
            listOf("-s", server, "-u", user, "-p", password, "-f", fileListPath)
        )

        verify(exactly = 1) { submissionService.validateFileList((validateRequest)) }
    }

    companion object {
        const val server = "server"
        const val user = "user"
        const val password = "password"
        const val fileListPath = "file-list.json"
        private val securityConfig = SecurityConfig(server, user, password)
        val validateRequest = ValidateFileListRequest(securityConfig, fileListPath)
    }
}
