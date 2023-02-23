package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import ebi.ac.uk.extended.model.StorageMode
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.TransferenceParameters.ACC_NO
import uk.ac.ebi.biostd.client.cli.common.TransferenceParameters.TARGET
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.TransferRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class TransferCommand(
    private val subService: SubmissionService
) : CliktCommand(name = "transfer") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val accNo by option("-ac", "--accNo", help = ACC_NO).required()
    private val target by option("-t", "--target", help = TARGET).required()

    override fun run() {
        val targetStorage = StorageMode.fromString(target)
        val securityConfig = SecurityConfig(server, user, password)

        subService.transfer(TransferRequest(accNo, targetStorage, securityConfig))
        echo("SUCCESS: Submission with AccNo $accNo is in queue to be transferred")
    }
}
