package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.ACC_NO_HELP
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class DeleteCommand(
    private val submissionService: SubmissionService,
) : CliktCommand(name = "delete") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val accNoList: List<String> by argument("--accNo", help = ACC_NO_HELP).multiple(required = true)

    @Suppress("TooGenericExceptionCaught")
    override fun run(): Unit =
        runBlocking {
            val securityConfig = SecurityConfig(server, user, password, onBehalf)
            submissionService.delete(securityConfig, accNoList)
            echo("SUCCESS: Submission with AccNo ${accNoList.joinToString()} was deleted")
        }
}
