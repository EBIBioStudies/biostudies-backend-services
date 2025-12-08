package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import ebi.ac.uk.extended.model.StorageMode
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.ACC_NO
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.TARGET
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class MigrateCommand(
    private val submissionService: SubmissionService,
) : CliktCommand(name = "migrate") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val accNo by option("-ac", "--accNo", help = ACC_NO).required()
    private val target by option("-t", "--target", help = TARGET).required()

    override fun run(): Unit =
        runBlocking {
            val targetStorage = StorageMode.fromString(target)
            val securityConfig = SecurityConfig(server, user, password)
            submissionService.migrate(securityConfig, accNo, targetStorage)
            echo("SUCCESS: Submission with AccNo $accNo is in queue to be migrated")
        }
}
