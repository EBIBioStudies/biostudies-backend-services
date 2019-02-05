package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.data.MongoDocService
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val submissionDocService: MongoDocService,
    private val pmcImporterProperties: PmcImporterProperties
) {
    suspend fun submit() = submissionDocService
            .getNotImportedSubmissions(pmcImporterProperties.sourceFile)
            .map { GlobalScope.launch { processSubmission(it) } }

    private suspend fun processSubmission(submission: SubmissionDoc) = coroutineScope {
        try {
            val files = submissionDocService.getSubFiles(submission.files).map { File(it.path) }.toList()

            bioWebClient.submitSingle(submission.body.replace("\\", ""), SubmissionFormat.JSON, files)
            submissionDocService.markAsImported(submission)

            logger.info { "Submission ${submission.id} successfully submitted" }
        } catch (exception: Exception) {
            logger.error { "Errors processing ${submission.id}" }
            submissionDocService.reportError(submission, exception)
        }
    }
}
