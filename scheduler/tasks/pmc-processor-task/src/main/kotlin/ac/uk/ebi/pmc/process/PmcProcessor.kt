package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.process.util.FileDownloader
import ac.uk.ebi.pmc.process.util.SubmissionInitializer
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

private const val BUFFER_SIZE = 30

class PmcProcessor(
    private val errorDocService: ErrorsDocService,
    private val submissionInitializer: SubmissionInitializer,
    private val submissionDocService: SubmissionDocService,
    private val fileDownloader: FileDownloader,
) {
    fun processAll(sourceFile: String?): Unit = runBlocking { processAllSubmissions(sourceFile) }

    private suspend fun processAllSubmissions(sourceFile: String?) {
        supervisorScope {
            submissionDocService.findReadyToProcess(sourceFile)
                .concurrently(BUFFER_SIZE) { processSubmission(it) }
                .collect()
        }
    }

    private suspend fun processSubmission(submissionDoc: SubmissionDoc) {
        runCatching { submissionInitializer.getSubmission(submissionDoc.body) }
            .fold(
                { (submission, body) -> downloadFiles(submission, body, submissionDoc) },
                { errorDocService.saveError(submissionDoc, PmcMode.PROCESS, it) },
            )
    }

    private suspend fun downloadFiles(
        submission: Submission,
        subBody: String,
        submissionDoc: SubmissionDoc,
    ) {
        fileDownloader.downloadFiles(submission).fold(
            { submissionDocService.saveProcessedSubmission(submissionDoc.withBody(subBody), it) },
            { errorDocService.saveError(submissionDoc, PmcMode.PROCESS, it) },
        )
    }
}
