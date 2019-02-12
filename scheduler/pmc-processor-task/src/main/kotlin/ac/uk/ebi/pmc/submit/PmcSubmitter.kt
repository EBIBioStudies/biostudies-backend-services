package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import arrow.core.Try
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val docService: MongoDocService,
    private val submissionService: SubmissionDocService
) {

    suspend fun submit(): List<Job> {
        var submission = submissionService.getReadyToSubmit()
        val jobs = mutableListOf<Job>()

        while (submission.isDefined()) {
            jobs.add(GlobalScope.launch { processSubmission(submission.get()) })
            submission = submissionService.getReadyToSubmit()
        }

        return jobs
    }

    private suspend fun processSubmission(submission: SubmissionDoc) = coroutineScope {
        Try {
            val files = submissionService.getSubFiles(submission.files).map { File(it.path) }.toList()
            bioWebClient.submitSingle(submission.body.replace("\\", ""), SubmissionFormat.JSON, files)
        }.fold(
            { docService.saveError(submission, it) },
            { submissionService.changeStatus(submission, SubmissionStatus.SUBMITTED) }
        )
    }
}
