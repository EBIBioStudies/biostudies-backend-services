package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.data.MongoDocService
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val submissionDocService: MongoDocService
) {
    suspend fun submit() =
        submissionDocService.getAllSubmissions().map { GlobalScope.launch { processSubmission(it) } }

    private suspend fun processSubmission(submission: SubmissionDoc) = coroutineScope {
        logger.info { "Submitting ${submission.id}" }

        val files = submissionDocService.getSubFiles(submission.files).map { File(it.path) }.toList()
        val response = bioWebClient.submitSingle(submission.body.replace("\\", ""), SubmissionFormat.JSON, files)

        logger.info { "Submission request response for ${submission.id}: ${response.statusCode}" }

    }
}
