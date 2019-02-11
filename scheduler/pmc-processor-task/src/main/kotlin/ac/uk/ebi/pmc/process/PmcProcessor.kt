package ac.uk.ebi.pmc.process

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PmcProcessor(
    private val subDocService: MongoDocService,
    private val serializationService: SerializationService,
    private val fileDownloader: FileDownloader
) {

    suspend fun processSubmissions(): List<Job> {
        var submission = subDocService.getReadyToProcess()
        val jobs = mutableListOf<Job>()

        while (submission.isDefined()) {
            jobs.add(GlobalScope.launch { processSubmission(submission.get()) })
            submission = subDocService.getReadyToProcess()
        }

        return jobs
    }

    private suspend fun processSubmission(submissionDoc: SubmissionDoc): Submission {
        val submission = serializationService.deserializeSubmission(submissionDoc.body, SubFormat.JSON)
        fileDownloader.downloadFiles(submission).fold(
            { subDocService.saveError(submissionDoc, it) },
            { subDocService.saveSubmission(submission, submissionDoc.sourceFile, it) })
        return submission
    }
}
