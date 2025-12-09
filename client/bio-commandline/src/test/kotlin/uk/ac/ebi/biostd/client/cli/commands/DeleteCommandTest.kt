package uk.ac.ebi.biostd.client.cli.commands

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class)
internal class DeleteCommandTest(
    @param:MockK private val submissionService: SubmissionService,
) {
    private val testInstance = DeleteCommand(submissionService)

    @Test
    fun run() {
        coEvery { submissionService.delete(securityConfig, listOf(ACC_NO1, ACC_NO2)) } returns Unit

        testInstance.parse(listOf("-s", SERVER, "-u", USER, "-p", PASSWORD, ACC_NO1, ACC_NO2))

        coVerify(exactly = 1) { submissionService.delete(securityConfig, listOf(ACC_NO1, ACC_NO2)) }
    }

    private companion object {
        const val SERVER = "server"
        const val USER = "user"
        const val PASSWORD = "password"
        const val ACC_NO1 = "accNo1"
        const val ACC_NO2 = "accNo2"

        val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
    }
}
