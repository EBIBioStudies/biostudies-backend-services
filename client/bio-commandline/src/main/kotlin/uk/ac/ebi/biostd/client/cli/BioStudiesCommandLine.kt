package uk.ac.ebi.biostd.client.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.ac.ebi.biostd.client.cli.commands.DeleteCommand
import uk.ac.ebi.biostd.client.cli.commands.DeleteUserFilesCommand
import uk.ac.ebi.biostd.client.cli.commands.GenerateDoiCommand
import uk.ac.ebi.biostd.client.cli.commands.GrantPermissionCommand
import uk.ac.ebi.biostd.client.cli.commands.MigrateCommand
import uk.ac.ebi.biostd.client.cli.commands.RevokePermissionCommand
import uk.ac.ebi.biostd.client.cli.commands.SubmissionRequestStatusCommand
import uk.ac.ebi.biostd.client.cli.commands.SubmitCommand
import uk.ac.ebi.biostd.client.cli.commands.TransferCommand
import uk.ac.ebi.biostd.client.cli.commands.UploadUserFilesCommand
import uk.ac.ebi.biostd.client.cli.commands.ValidateFileListCommand
import uk.ac.ebi.biostd.client.cli.services.SecurityService
import uk.ac.ebi.biostd.client.cli.services.SubmissionRequestService
import uk.ac.ebi.biostd.client.cli.services.SubmissionService
import uk.ac.ebi.biostd.client.cli.services.UserFilesService

internal class BioStudiesCommandLine : CliktCommand() {
    override fun run() {
        echo("BioStudies CLI")
    }
}

fun main(args: Array<String>) {
    val userFilesService = UserFilesService()
    val submissionService = SubmissionService()
    val securityService = SecurityService()
    val subRequestService = SubmissionRequestService()

    BioStudiesCommandLine()
        .subcommands(
            SubmitCommand(submissionService),
            DeleteCommand(submissionService),
            MigrateCommand(submissionService),
            TransferCommand(submissionService),
            GenerateDoiCommand(submissionService),
            GrantPermissionCommand(securityService),
            RevokePermissionCommand(securityService),
            DeleteUserFilesCommand(userFilesService),
            UploadUserFilesCommand(userFilesService),
            ValidateFileListCommand(submissionService),
            SubmissionRequestStatusCommand(subRequestService),
        ).main(args)
}
