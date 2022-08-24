package uk.ac.ebi.biostd.client.cli.commands

import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import ebi.ac.uk.extended.model.StorageMode
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.ATTACHED_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.INPUT_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.PREFERRED_SOURCES
import uk.ac.ebi.biostd.client.cli.common.splitFiles
import uk.ac.ebi.biostd.client.cli.common.splitPreferredSources
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class SubmitAsyncCommand(
    private val submissionService: SubmissionService,
) : CliktCommand(name = "submitAsync") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val input by option("-i", "--input", help = INPUT_HELP).file(exists = true).required()
    private val attached by option("-a", "--attached", help = ATTACHED_HELP)
    private val preferredSources by option("-ps", "--preferredSources", help = PREFERRED_SOURCES)
    private val storageMode by option("-sm", "--storageMode", help = PREFERRED_SOURCES)

    override fun run() {
        val mode = storageMode?.let { StorageMode.fromString(it) }
        val securityConfig = SecurityConfig(server, user, password, onBehalf)
        val filesConfig = SubmissionFilesConfig(splitFiles(attached), splitPreferredSources(preferredSources))

        submissionService.submitAsync(SubmissionRequest(input, mode, securityConfig, filesConfig))
        echo("SUCCESS: Submission is in queue to be submitted")
    }
}
