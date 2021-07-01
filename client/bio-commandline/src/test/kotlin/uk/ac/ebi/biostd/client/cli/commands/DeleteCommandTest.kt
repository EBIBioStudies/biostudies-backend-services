package uk.ac.ebi.biostd.client.cli.commands

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class)
internal class DeleteCommandTest(
    @MockK private val submissionService: SubmissionService
) {
    private val testInstance = DeleteCommand(submissionService)

    @Test
    fun run() {
        every { submissionService.delete(subDelete) } returns Unit

        testInstance.parse(listOf("-s", server, "-u", user, "-p", password, accNo1, accNo2))

        verify(exactly = 1) { submissionService.delete(subDelete) }
    }

    companion object {
        const val server = "server"
        const val user = "user"
        const val password = "password"
        const val accNo1 = "accNo1"
        const val accNo2 = "accNo2"
        val subDelete = DeletionRequest(
            server = server,
            user = user,
            password = password,
            onBehalf = null,
            accNoList = listOf(accNo1, accNo2)
        )
    }
}
