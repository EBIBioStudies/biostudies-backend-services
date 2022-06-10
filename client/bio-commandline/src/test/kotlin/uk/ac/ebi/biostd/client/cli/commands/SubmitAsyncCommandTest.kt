package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.IncorrectOptionValueCount
import com.github.ajalt.clikt.core.MissingParameter
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.FileMode.MOVE
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
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
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class SubmitAsyncCommandTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val submissionService: SubmissionService
) {
    private var testInstance = SubmitAsyncCommand(submissionService)
    private val rootFolder: String = temporaryFolder.root.absolutePath

    @AfterEach
    fun afterEach() {
        clearAllMocks()
        temporaryFolder.clean()
    }

    @Test
    fun `submit async successful`() {
        val submission = temporaryFolder.createFile("Submission.tsv")
        val attachedFile1 = temporaryFolder.createFile("attachedFile1.tsv")
        val attachedFile2 = temporaryFolder.createFile("attachedFile2.tsv")

        val securityConfig = SecurityConfig("server", "user", "password")
        val request = SubmissionRequest(
            securityConfig,
            file = submission,
            attached = listOf(attachedFile1, attachedFile2),
            fileMode = COPY,
            preferredSource = USER_SPACE,
        )
        every { submissionService.submitAsync(request) } answers { nothing }

        testInstance.parse(
            listOf(
                "-s", "server",
                "-u", "user",
                "-p", "password",
                "-i", "$rootFolder/Submission.tsv",
                "-a", "$rootFolder/attachedFile1.tsv,$rootFolder/attachedFile2.tsv"
            )
        )

        verify(exactly = 1) { submissionService.submitAsync(request) }
    }

    @Test
    fun `submit async successful moving files`() {
        val submission = temporaryFolder.createFile("Submission.tsv")
        val attachedFile1 = temporaryFolder.createFile("attachedFile1.tsv")
        val attachedFile2 = temporaryFolder.createFile("attachedFile2.tsv")

        val securityConfig = SecurityConfig("server", "user", "password")
        val request = SubmissionRequest(
            securityConfig,
            file = submission,
            attached = listOf(attachedFile1, attachedFile2),
            fileMode = MOVE,
            preferredSource = SUBMISSION,
        )
        every { submissionService.submitAsync(request) } answers { nothing }

        testInstance.parse(
            listOf(
                "-s", "server",
                "-u", "user",
                "-p", "password",
                "-i", "$rootFolder/Submission.tsv",
                "-a", "$rootFolder/attachedFile1.tsv,$rootFolder/attachedFile2.tsv",
                "-fm", "MOVE",
                "-ps", "SUBMISSION"
            )
        )

        verify(exactly = 1) { submissionService.submitAsync(request) }
    }

    @Test
    fun `no attached files`() {
        val submission = temporaryFolder.createFile("Submission.tsv")

        val securityConfig = SecurityConfig("server", "user", "password")
        val request = SubmissionRequest(
            securityConfig,
            file = submission,
            attached = emptyList(),
            fileMode = COPY,
            preferredSource = USER_SPACE,
        )
        every { submissionService.submitAsync(request) } answers { nothing }

        testInstance.parse(
            listOf(
                "-s", "server",
                "-u", "user",
                "-p", "password",
                "-i", "$rootFolder/Submission.tsv"
            )
        )

        verify(exactly = 1) { submissionService.submitAsync(request) }
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
