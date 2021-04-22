package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.IncorrectOptionValueCount
import com.github.ajalt.clikt.core.MissingParameter
import ebi.ac.uk.model.Submission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class SubmitCommandTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val submissionService: SubmissionService
) {
    private lateinit var rootFolder: String
    private var testInstance = SubmitCommand(submissionService)

    private val server = "server"
    private val user = "user"
    private val password = "password"

    @Test
    fun `submit successful`() {
        rootFolder = temporaryFolder.root.absolutePath
        val mockResponse = Submission("S-TEST123")

        val submission = temporaryFolder.createFile("Submission.tsv")
        val attachedFile1 = temporaryFolder.createFile("attachedFile1.tsv")
        val attachedFile2 = temporaryFolder.createFile("attachedFile2.tsv")

        val request = SubmissionRequest(
            server = server,
            user = user,
            password = password,
            onBehalf = null,
            file = submission,
            attached = listOf(attachedFile1, attachedFile2)
        )
        every { submissionService.submit(request) } returns mockResponse

        testInstance.parse(
            listOf(
                "-s", server,
                "-u", user,
                "-p", password,
                "-i", "$rootFolder/Submission.tsv",
                "-a", "$rootFolder/attachedFile1.tsv,$rootFolder/attachedFile2.tsv"
            )
        )

        verify(exactly = 1) { submissionService.submit(request) }
    }

    @Test
    fun `missing arguments`() {
        val exceptionMessage = assertThrows<MissingParameter> { testInstance.parse(listOf("-u", "manager")) }.message
        assertThat(exceptionMessage).isEqualTo("Missing option \"--server\".")
    }

    @Test
    fun `missing value for argument`() {
        val exceptionMessage = assertThrows<IncorrectOptionValueCount> { testInstance.parse(listOf("-u")) }.message
        assertThat(exceptionMessage).isEqualTo("-u option requires an argument")
    }
}
