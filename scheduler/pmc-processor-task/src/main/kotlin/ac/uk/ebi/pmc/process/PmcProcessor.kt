package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.config.MAX_CONNECTIONS
import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.process.util.FileDownloader
import ac.uk.ebi.pmc.process.util.SubmissionInitializer
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PmcProcessor(
    private val errorDocService: ErrorsDocService,
    private val submissionInitializer: SubmissionInitializer,
    private val submissionDocService: SubmissionDocService,
    private val fileDownloader: FileDownloader
) {

    suspend fun processSubmissions() = withContext(Dispatchers.Default) {
        val receiveChannel = launchProducer()
        (1..MAX_CONNECTIONS * 3).map { launchProcessor(receiveChannel) }.joinAll()
    }

    private fun CoroutineScope.launchProcessor(channel: ReceiveChannel<SubmissionDoc>) =
        launch { for (submission in channel) processSubmission(submission) }

    private fun CoroutineScope.launchProducer() = produce {
        submissionDocService.findReadyToProcess().forEach { send(it) }
        close()
    }

    private suspend fun processSubmission(submissionDoc: SubmissionDoc): Submission {
        val submission = submissionInitializer.getSubmission(submissionDoc.body)
        fileDownloader.downloadFiles(submission).fold(
            { errorDocService.saveError(submissionDoc, PmcMode.PROCESS, it) },
            { submissionDocService.saveProcessedSubmission(submissionDoc, it) })

        logger.info { "Finishing processing of submission with accNo = '${submission.accNo}'" }
        return submission
    }
}
