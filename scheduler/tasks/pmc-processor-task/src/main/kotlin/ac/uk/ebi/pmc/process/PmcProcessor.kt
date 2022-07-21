package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.process.util.FileDownloader
import ac.uk.ebi.pmc.process.util.SubmissionInitializer
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private const val BUFFER_SIZE = 30

class PmcProcessor(
    private val errorDocService: ErrorsDocService,
    private val submissionInitializer: SubmissionInitializer,
    private val submissionDocService: SubmissionDocService,
    private val fileDownloader: FileDownloader,
) {

    fun processAll() {
        runBlocking { processSubmissions() }
    }

    private suspend fun processSubmissions() = withContext(Dispatchers.Default) {
        submissionDocService.findReadyToProcess()
            .map { async { processSubmission(it) } }
            .buffer(BUFFER_SIZE)
            .collect { it.await() }
    }

    private suspend fun processSubmission(submissionDoc: SubmissionDoc) {
        runCatching { submissionInitializer.getSubmission(submissionDoc.body) }
            .fold(
                { downloadFiles(it, submissionDoc) },
                { errorDocService.saveError(submissionDoc, PmcMode.PROCESS, it) }
            )
    }

    private suspend fun downloadFiles(submissionPair: Pair<Submission, String>, submissionDoc: SubmissionDoc) {
        val (submission, body) = submissionPair
        fileDownloader.downloadFiles(submission).fold(
            { errorDocService.saveError(submissionDoc, PmcMode.PROCESS, it) },
            { submissionDocService.saveProcessedSubmission(submissionDoc.withBody(body), it) }
        )
    }
}
