package uk.ac.ebi.biostd.client.cli.commands

import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.StorageMode.FIRE
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.ATTACHED_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.INPUT_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.PREFERRED_SOURCES
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.STORAGE_MODE
import uk.ac.ebi.biostd.client.cli.common.splitFiles
import uk.ac.ebi.biostd.client.cli.common.splitPreferredSources
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@Deprecated("This command will be deprecated in favor of submitAsync")
internal class SubmitCommand(
    private val subService: SubmissionService,
) : CliktCommand(name = "submit") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val input by option("-i", "--input", help = INPUT_HELP).file(exists = true).required()
    private val attached by option("-a", "--attached", help = ATTACHED_HELP)
    private val preferredSources by option("-ps", "--preferredSources", help = PREFERRED_SOURCES)
    private val storageMode by option("-sm", "--storageMode", help = STORAGE_MODE).default(FIRE.value)

    override fun run() {
        val mode = StorageMode.fromString(storageMode)
        val securityConfig = SecurityConfig(server, user, password, onBehalf)
        val filesConfig = SubmissionFilesConfig(splitFiles(attached), mode, splitPreferredSources(preferredSources))

        val response = subService.submit(SubmissionRequest(input, securityConfig, filesConfig))
        echo("WARNING: This command will be DEPRECATED. Please start using submitAsync instead")
        echo("SUCCESS: Submission with AccNo ${response.accNo} was submitted")
    }
}
