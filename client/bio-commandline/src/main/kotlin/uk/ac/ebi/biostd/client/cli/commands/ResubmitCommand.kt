package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.model.constants.ACC_NO
import kotlinx.coroutines.runBlocking
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.ON_BEHALF_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.PASSWORD_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.SERVER_HELP
import uk.ac.ebi.biostd.client.cli.common.CommonParameters.USER_HELP
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.AWAIT
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.SINGLE_JOB
import uk.ac.ebi.biostd.client.cli.common.SubmissionParameters.STORAGE_MODE
import uk.ac.ebi.biostd.client.cli.dto.ResubmissionRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

internal class ResubmitCommand(
    private val submissionService: SubmissionService,
) : CliktCommand(name = "resubmit") {
    private val server by option("-s", "--server", help = SERVER_HELP).required()
    private val user by option("-u", "--user", help = USER_HELP).required()
    private val password by option("-p", "--password", help = PASSWORD_HELP).required()
    private val onBehalf by option("-b", "--onBehalf", help = ON_BEHALF_HELP)
    private val accNo by option("-ac", "--accNo", help = ACC_NO).required()
    private val storageMode by option("-sm", "--storageMode", help = STORAGE_MODE).default(FIRE.value)
    private val singleJob by option("-sj", "--singleJob", help = SINGLE_JOB).flag(default = true)
    private val await by option("-aw", "--await", help = AWAIT).flag(default = false)

    override fun run() {
        runBlocking { resubmit() }
    }

    private suspend fun resubmit() {
        val mode = StorageMode.fromString(storageMode)
        val securityConfig = SecurityConfig(server, user, password, onBehalf)
        val params = SubmitParameters(
            storageMode = mode,
            singleJobMode = singleJob,
            preferredSources = listOf(SUBMISSION),
        )
        submissionService.resubmit(ResubmissionRequest(accNo, await, securityConfig, params))
    }
}
