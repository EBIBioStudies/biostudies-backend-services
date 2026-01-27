package uk.ac.ebi.biostd.client.cli.commands

import ebi.ac.uk.model.SubmissionTransferOptions
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class)
internal class TransferCommandTest(
    @param:MockK private val submissionService: SubmissionService,
) {
    private val testInstance = TransferCommand(submissionService)

    @Test
    fun `transfer all submissions`() {
        val options = SubmissionTransferOptions(OWNER, TARGET_OWNER)
        coEvery { submissionService.transfer(securityConfig, options) } returns Unit

        testInstance.parse(listOf("-s", SERVER, "-u", USER, "-p", PASSWORD, "-o", OWNER, "-to", TARGET_OWNER))

        coVerify(exactly = 1) { submissionService.transfer(securityConfig, options) }
    }

    @Test
    fun `transfer specific submissions`() {
        val subs = listOf(ACC_NO1, ACC_NO2)
        val options = SubmissionTransferOptions(OWNER, TARGET_OWNER, USER_NAME, subs)
        coEvery { submissionService.transfer(securityConfig, options) } returns Unit

        testInstance.parse(
            listOf(
                "-s",
                SERVER,
                "-u",
                USER,
                "-p",
                PASSWORD,
                "-o",
                OWNER,
                "-to",
                TARGET_OWNER,
                "-un",
                USER_NAME,
                ACC_NO1,
                ACC_NO2,
            ),
        )

        coVerify(exactly = 1) { submissionService.transfer(securityConfig, options) }
    }

    private companion object {
        const val SERVER = "server"
        const val USER = "user"
        const val PASSWORD = "password"
        const val OWNER = "owner"
        const val TARGET_OWNER = "target"
        const val USER_NAME = "userName"
        const val ACC_NO1 = "accNo1"
        const val ACC_NO2 = "accNo2"

        val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
    }
}
