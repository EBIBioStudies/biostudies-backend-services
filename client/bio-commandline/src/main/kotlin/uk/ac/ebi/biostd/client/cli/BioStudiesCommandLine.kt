package uk.ac.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.exception.WebClientException
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.subcommands
import uk.ac.ebi.biostd.client.cli.BioStudiesCommandLine.Companion.OPERATIONS
import uk.ac.ebi.biostd.client.cli.commands.DeleteCommand
import uk.ac.ebi.biostd.client.cli.commands.DeleteUserFilesCommand
import uk.ac.ebi.biostd.client.cli.commands.GenerateDoiCommand
import uk.ac.ebi.biostd.client.cli.commands.GrantPermissionCommand
import uk.ac.ebi.biostd.client.cli.commands.MigrateCommand
import uk.ac.ebi.biostd.client.cli.commands.RevokePermissionCommand
import uk.ac.ebi.biostd.client.cli.commands.SubmissionRequestStatusCommand
import uk.ac.ebi.biostd.client.cli.commands.SubmitCommand
import uk.ac.ebi.biostd.client.cli.commands.TransferCommand
import uk.ac.ebi.biostd.client.cli.commands.UpdateUserEmailCommand
import uk.ac.ebi.biostd.client.cli.commands.UploadUserFilesCommand
import uk.ac.ebi.biostd.client.cli.config.DataMoverConfig
import uk.ac.ebi.biostd.client.cli.services.DataMoverUserFilesService
import uk.ac.ebi.biostd.client.cli.services.SecurityService
import uk.ac.ebi.biostd.client.cli.services.SubmissionRequestService
import uk.ac.ebi.biostd.client.cli.services.SubmissionService
import uk.ac.ebi.biostd.client.cli.services.UserFilesService

internal class BioStudiesCommandLine : CliktCommand() {
    override fun run() {
        echo("BioStudies CLI")
    }

    companion object {
        val OPERATIONS =
            listOf(
                "submit",
                "delete",
                "migrate",
                "transfer",
                "generateDoi",
                "grantPermission",
                "revokePermission",
                "deleteUserFiles",
                "uploadUserFiles",
                "updateUserEmail",
                "requestStatus",
            )
    }
}

fun main(args: Array<String>) {
    installExceptionHandler()
    validateOperation(args)

    val datamoverService = DataMoverUserFilesService(DataMoverConfig.load())
    val userFilesService = UserFilesService(datamoverService)
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
            UpdateUserEmailCommand(submissionService),
            SubmissionRequestStatusCommand(subRequestService),
        ).main(args)
}

private fun validateOperation(args: Array<String>) {
    val operation = args.first()
    require(OPERATIONS.contains(operation)) { throw CliktError("Error: Invalid operation: \"$operation\".") }
}

private fun installExceptionHandler() {
    val thread = Thread.currentThread()
    val defaultHandler = thread.uncaughtExceptionHandler
    thread.uncaughtExceptionHandler =
        Thread.UncaughtExceptionHandler { currentThread, error ->
            when (error) {
                is CliktError,
                is WebClientException,
                -> System.err.println(error)

                else -> defaultHandler.uncaughtException(currentThread, error)
            }
        }
}
