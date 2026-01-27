package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import ebi.ac.uk.model.SubmissionTransferOptions
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.TransferenceParameters.ACC_NO_LIST
import uk.ac.ebi.biostd.client.cli.common.TransferenceParameters.OWNER
import uk.ac.ebi.biostd.client.cli.common.TransferenceParameters.TARGET_OWNER
import uk.ac.ebi.biostd.client.cli.common.TransferenceParameters.USER_NAME
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class TransferCommand(
    private val subService: SubmissionService,
) : CliktCommand(name = "transfer") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val owner by option("-o", "--owner", help = OWNER).required()
    private val targetOwner by option("-to", "--targetOwner", help = TARGET_OWNER).required()
    private val userName by option("-un", "--userName", help = USER_NAME)
    private val accNoList: List<String> by argument("--accNo", help = ACC_NO_LIST).multiple(required = false)

    override fun run(): Unit =
        runBlocking {
            val securityConfig = SecurityConfig(server, user, password)
            val options = SubmissionTransferOptions(owner, targetOwner, userName, accNoList)

            subService.transfer(securityConfig, options)
            echo("SUCCESS: Submissions transferred from user '$owner' to '$targetOwner'")
        }
}
