package uk.ac.ebi.biostd.client.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.ac.ebi.biostd.client.cli.commands.DeleteCommand
import uk.ac.ebi.biostd.client.cli.commands.GrantCollectionPermissionCommand
import uk.ac.ebi.biostd.client.cli.commands.GrantSubmissionPermissionCommand
import uk.ac.ebi.biostd.client.cli.commands.MigrateCommand
import uk.ac.ebi.biostd.client.cli.commands.SubmitAsyncCommand
import uk.ac.ebi.biostd.client.cli.commands.SubmitCommand
import uk.ac.ebi.biostd.client.cli.commands.TransferCommand
import uk.ac.ebi.biostd.client.cli.commands.ValidateFileListCommand
import uk.ac.ebi.biostd.client.cli.services.SecurityService
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class BioStudiesCommandLine : CliktCommand() {
    override fun run() {
        echo("BioStudies CLI")
    }
}

fun main(args: Array<String>) {
    val service = SubmissionService()
    val securityService = SecurityService()
    BioStudiesCommandLine()
        .subcommands(
            SubmitCommand(service),
            SubmitAsyncCommand(service),
            DeleteCommand(service),
            MigrateCommand(service),
            TransferCommand(service),
            GrantCollectionPermissionCommand(securityService),
            GrantSubmissionPermissionCommand(securityService),
            ValidateFileListCommand(service),
        )
        .main(args)
}
