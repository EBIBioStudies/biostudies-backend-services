package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.StorageMode.FIRE
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.ATTACHED_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.AWAIT
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.INPUT_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.PREFERRED_SOURCES
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.SPLIT_JOBS
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.STORAGE_MODE
import uk.ac.ebi.biostd.client.cli.common.splitFiles
import uk.ac.ebi.biostd.client.cli.common.splitPreferredSources
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class SubmitCommand(
    private val submissionService: SubmissionService,
) : CliktCommand(name = "submit") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val input by option("-i", "--input", help = INPUT_HELP).file(exists = true).required()
    private val attached by option("-a", "--attached", help = ATTACHED_HELP)
    private val preferredSources by option("-ps", "--preferredSources", help = PREFERRED_SOURCES)
    private val storageMode by option("-sm", "--storageMode", help = STORAGE_MODE).default(FIRE.value)
    private val splitJobs by option("-sj", "--splitJobs", help = SPLIT_JOBS).flag(default = false)
    private val await by option("-aw", "--await", help = AWAIT).flag(default = false)

    override fun run() {
        runBlocking { submit() }
    }

    private suspend fun submit() {
        val mode = StorageMode.fromString(storageMode)
        val securityConfig = SecurityConfig(server, user, password, onBehalf)
        val params =
            SubmitParameters(
                storageMode = mode,
                singleJobMode = splitJobs.not(),
                preferredSources = splitPreferredSources(preferredSources),
            )
        val files = splitFiles(attached)
        submissionService.submit(SubmissionRequest(input, await, securityConfig, params, files))
    }
}
