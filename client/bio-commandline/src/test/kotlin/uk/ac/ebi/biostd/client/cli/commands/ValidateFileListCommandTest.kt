package uk.ac.ebi.biostd.client.cli.commands

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
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
            listOf(
                "-s", server,
                "-u", user,
                "-p", password,
                "-f", fileListPath,
                "-ac", accNo,
                "-rp", rootPath
            )
        )

        verify(exactly = 1) { submissionService.validateFileList((validateRequest)) }
    }

    companion object {
        private const val server = "server"
        private const val user = "user"
        private const val password = "password"
        private const val fileListPath = "file-list.json"
        private const val rootPath = "root-path"
        private const val accNo = "S-BSST123"
        private val securityConfig = SecurityConfig(server, user, password)
        private val validateRequest = ValidateFileListRequest(fileListPath, accNo, rootPath, securityConfig)
    }
}
