package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import ac.uk.ebi.pmc.process.util.FileDownloader
import ac.uk.ebi.pmc.process.util.SubmissionInitializer
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

private const val CONCURRENCY = 30

class PmcProcessor(
    private val submissionInitializer: SubmissionInitializer,
    private val errorService: ErrorsService,
    private val submissionService: SubmissionService,
    private val fileDownloader: FileDownloader,
) {
    fun processAll(sourceFile: String?): Unit = runBlocking { processSubmissions(sourceFile) }

    private suspend fun processSubmissions(sourceFile: String?) {
        supervisorScope {
            submissionService.findReadyToProcess(sourceFile)
                .concurrently(CONCURRENCY) { processSubmission(it) }
                .collect()
        }
    }

    private suspend fun processSubmission(subDoc: SubmissionDocument) {
        runCatching { submissionInitializer.getSubmission(subDoc.body) }
            .fold(
                { (sub, subBody) -> downloadFiles(sub, subBody, subDoc) },
                { errorService.saveError(subDoc, PmcMode.PROCESS, it) },
            )
    }

    private suspend fun downloadFiles(
        submission: Submission,
        subBody: String,
        sub: SubmissionDocument,
    ) {
        fileDownloader.downloadFiles(submission).fold(
            { submissionService.saveProcessedSubmission(sub.copy(body = subBody), it) },
            { errorService.saveError(sub, PmcMode.PROCESS, it) },
        )
    }
}
