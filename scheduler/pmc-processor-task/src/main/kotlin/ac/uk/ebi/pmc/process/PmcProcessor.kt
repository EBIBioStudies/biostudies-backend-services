package ac.uk.ebi.pmc.process

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val WORKERS = 10

@ExperimentalCoroutinesApi
class PmcProcessor(
    private val mongoDocService: MongoDocService,
    private val serializationService: SerializationService,
    private val submissionDocService: SubmissionDocService,
    private val fileDownloader: FileDownloader
) {

    suspend fun processSubmissions() = withContext(Dispatchers.Default) {
        val receiveChannel = launchProducer()
        (1..WORKERS).map { launchProcessor(receiveChannel) }.joinAll()
    }

    private fun CoroutineScope.launchProcessor(channel: ReceiveChannel<SubmissionDoc>) =
        launch { for (submission in channel) processSubmission(submission) }

    private fun CoroutineScope.launchProducer() = produce {
        submissionDocService.findReadyToProcess().forEach { send(it) }
        close()
    }

    private suspend fun processSubmission(submissionDoc: SubmissionDoc): Submission {
        val submission = serializationService.deserializeSubmission(submissionDoc.body, SubFormat.JSON)
        fileDownloader.downloadFiles(submission).fold(
            { mongoDocService.saveError(submissionDoc, it) },
            { submissionDocService.saveProcessedSubmission(submissionDoc, it) })
        return submission
    }
}
