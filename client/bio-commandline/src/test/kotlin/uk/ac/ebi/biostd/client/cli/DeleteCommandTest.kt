package uk.ac.ebi.biostd.client.cli

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.commands.DeleteCommand
import uk.ac.ebi.biostd.client.cli.services.SubmissionDeleteRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class)
internal class DeleteCommandTest {
    private val submissionService: SubmissionService = mockk()
    private val testInstance = DeleteCommand(submissionService)

    @Test
    fun run() {
        every { submissionService.delete(subDelete) } returns Unit

        testInstance.parse(listOf("-s", server, "-u", user, "-p", password, "-ac", accNo))

        verify(exactly = 1) { submissionService.delete(subDelete) }
    }

    companion object {
        const val server = "server"
        const val user = "user"
        const val password = "password"
        const val accNo = "accNo"
        val subDelete = SubmissionDeleteRequest(
            server = server,
            user = user,
            password = password,
            onBehalf = null,
            accNo = accNo
        )
    }
}
