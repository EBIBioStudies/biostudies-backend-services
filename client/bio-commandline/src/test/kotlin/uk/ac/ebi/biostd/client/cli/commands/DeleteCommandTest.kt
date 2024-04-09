package uk.ac.ebi.biostd.client.cli.commands

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class)
internal class DeleteCommandTest(
    @MockK private val submissionService: SubmissionService,
) {
    private val testInstance = DeleteCommand(submissionService)

    @Test
    fun run() {
        every { submissionService.delete(subDelete) } returns Unit

        testInstance.parse(listOf("-s", SERVER, "-u", USER, "-p", PASSWORD, ACC_NO1, ACC_NO2))

        verify(exactly = 1) { submissionService.delete(subDelete) }
    }

    private companion object {
        const val SERVER = "server"
        const val USER = "user"
        const val PASSWORD = "password"
        const val ACC_NO1 = "accNo1"
        const val ACC_NO2 = "accNo2"

        val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
        val subDelete = DeletionRequest(securityConfig, listOf(ACC_NO1, ACC_NO2))
    }
}
