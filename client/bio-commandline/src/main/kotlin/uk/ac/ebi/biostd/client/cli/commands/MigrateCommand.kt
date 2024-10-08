package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.ACC_NO
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.ASYNC
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.SOURCE
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.SOURCE_PASSWORD
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.SOURCE_USER
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.TARGET
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.TARGET_OWNER
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.TARGET_PASSWORD
import uk.ac.ebi.biostd.client.cli.common.MigrationParameters.TARGET_USER
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

/**
 * Helps to migrate a submission from one environment into another.
 */
internal class MigrateCommand(
    private val submissionService: SubmissionService,
) : CliktCommand(name = "migrate") {
    private val accNo by option("-ac", "--accNo", help = ACC_NO).required()
    private val source by option("-s", "--source", help = SOURCE).required()
    private val sourceUser by option("-su", "--sourceUser", help = SOURCE_USER).required()
    private val sourcePassword by option("-sp", "--sourcePassword", help = SOURCE_PASSWORD).required()
    private val target by option("-t", "--target", help = TARGET).required()
    private val targetUser by option("-tu", "--targetUser", help = TARGET_USER).required()
    private val targetPassword by option("-tp", "--targetPassword", help = TARGET_PASSWORD).required()
    private val targetOwner by option("-to", "--targetOwner", help = TARGET_OWNER)
    private val async by option("-as", "--async", help = ASYNC).flag(default = false)

    override fun run(): Unit =
        runBlocking {
            val migrationRequest =
                MigrationRequest(
                    accNo = accNo,
                    sourceSecurityConfig = SecurityConfig(source, sourceUser, sourcePassword),
                    targetSecurityConfig = SecurityConfig(target, targetUser, targetPassword),
                    targetOwner = targetOwner,
                    async = async,
                )
            submissionService.migrate(migrationRequest)

            when (async) {
                true -> echo("SUCCESS: Submission with AccNo '$accNo' migration from $source to $target is in the queue")
                else -> echo("SUCCESS: Submission with AccNo '$accNo' was migrated from $source to $target")
            }
        }
}
