package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import arrow.core.Try
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val WORKERS = 3

@ExperimentalCoroutinesApi
class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val docService: MongoDocService,
    private val submissionService: SubmissionDocService
) {

    suspend fun submit() = withContext(Dispatchers.Default) {
        val receiveChannel = launchProducer()
        (1..WORKERS).map { launchProcessor(receiveChannel) }.joinAll()
    }

    private fun CoroutineScope.launchProcessor(channel: ReceiveChannel<SubmissionDoc>) =
        launch { for (submission in channel) processSubmission(submission) }

    private fun CoroutineScope.launchProducer() = produce {
        submissionService.findReadyToSubmit().forEach { send(it) }
        close()
    }

    private suspend fun processSubmission(submission: SubmissionDoc) = coroutineScope {
        Try {
            val files = submissionService.getSubFiles(submission.files).map { File(it.path) }.toList()
            bioWebClient.submitSingle(submission.body, SubmissionFormat.JSON, files)
        }.fold(
            { docService.saveError(submission, it) },
            { submissionService.changeStatus(submission, SubmissionStatus.SUBMITTED) }
        )
    }
}
