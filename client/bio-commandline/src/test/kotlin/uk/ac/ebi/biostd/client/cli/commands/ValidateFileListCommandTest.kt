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
    @MockK private val submissionService: SubmissionService,
) {
    private var testInstance = ValidateFileListCommand(submissionService)

    @Test
    fun `validate successful`() {
        every { submissionService.validateFileList(validateRequest) } returns Unit

        testInstance.parse(
            listOf(
                "-s", SERVER,
                "-u", USER,
                "-p", PASSWORD,
                "-f", FILE_LIST_PATH,
                "-ac", ACC_NO,
                "-rp", ROOT_PATH,
            ),
        )

        verify(exactly = 1) { submissionService.validateFileList((validateRequest)) }
    }

    private companion object {
        const val SERVER = "server"
        const val USER = "user"
        const val PASSWORD = "password"
        const val FILE_LIST_PATH = "file-list.json"
        const val ROOT_PATH = "root-path"
        const val ACC_NO = "S-BSST123"
        val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
        val validateRequest = ValidateFileListRequest(FILE_LIST_PATH, ACC_NO, ROOT_PATH, securityConfig)
    }
}
