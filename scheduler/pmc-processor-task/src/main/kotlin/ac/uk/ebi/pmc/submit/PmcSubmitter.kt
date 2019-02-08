package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.data.MongoDocService
import ac.uk.ebi.pmc.data.docs.SubStatus
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import arrow.core.Try
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val subDocService: MongoDocService
) {

    suspend fun submit(): List<Job> {
        var submission = subDocService.getReadyToSubmit()
        val jobs = mutableListOf<Job>()

        while (submission.isDefined()) {
            jobs.add(GlobalScope.launch { processSubmission(submission.get()) })
            submission = subDocService.getReadyToProcess()
        }

        return jobs
    }

    private suspend fun processSubmission(submission: SubmissionDoc) = coroutineScope {
        Try {
            val files = subDocService.getSubFiles(submission.files).map { File(it.path) }.toList()
            bioWebClient.submitSingle(submission.body.replace("\\", ""), SubmissionFormat.JSON, files)
        }.fold(
            { subDocService.saveError(submission, it) },
            { subDocService.markAs(submission, SubStatus.SUBMITTED) }
        )
    }
}
