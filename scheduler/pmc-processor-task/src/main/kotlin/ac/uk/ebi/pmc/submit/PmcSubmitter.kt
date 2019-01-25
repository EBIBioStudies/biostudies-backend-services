package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.pmc.data.MongoDocService
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PmcSubmitter(
    private val bioWebClient: BioWebClient,
    private val submissionDocService: MongoDocService,
    private val serializationService: SerializationService
) {
    suspend fun submit() =
        submissionDocService.getAllSubmissions().map { GlobalScope.launch { processSubmission(it) } }

    private suspend fun processSubmission(submission: SubmissionDoc) = coroutineScope {
        val deserialized = serializationService.deserializeSubmission(submission.body.replace("\\", ""), SubFormat.JSON)
        logger.info { "to submit ${deserialized.accNo}" }
        val response = bioWebClient.submitSingle(submission.body.replace("\\", ""), SubmissionFormat.JSON)
        logger.info { "submission response ${response.statusCode}" }
        submissionDocService.getSubFiles(submission.files).forEach {
            it.fold({ logger.info { "throw exception" } }, { logger.info { "upload ${ it.name }" } })
        }
    }
}
