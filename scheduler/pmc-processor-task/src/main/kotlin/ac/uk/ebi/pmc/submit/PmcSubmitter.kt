package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.scheduler.properties.PmcMode
import arrow.core.Try
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}
private const val WORKERS = 30

class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val errorDocService: ErrorsDocService,
    private val submissionService: SubmissionDocService
) {

    suspend fun submit() = withContext(Dispatchers.Default) {
        val receiveChannel = launchProducer()
        (1..WORKERS).map { launchProcessor(receiveChannel) }
    }

    private fun CoroutineScope.launchProcessor(channel: ReceiveChannel<SubmissionDoc>) =
        launch { for (submission in channel) submitSubmission(submission) }

    private fun CoroutineScope.launchProducer() = produce {
        submissionService.findReadyToSubmit().forEach { send(it) }
        close()
    }

    private suspend fun submitSubmission(submission: SubmissionDoc) = coroutineScope {
        Try {
            logger.info { "submitting accNo='${submission.accNo}'" }
            val files = submissionService.getSubFiles(submission.files).map { File(it.path) }
            bioWebClient.submitSingle(submission.body, SubmissionFormat.JSON, files)
        }.fold(
            { errorDocService.saveError(submission, PmcMode.SUBMIT, it) },
            { submissionService.changeStatus(submission, SubmissionStatus.SUBMITTED) }
        )
    }
}
