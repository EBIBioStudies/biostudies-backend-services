package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.data.MongoDocService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PmcSubmitter(
    private val submissionDocService: MongoDocService,
    private val serializationService: SerializationService
) {
    suspend fun submit() {
        submissionDocService.getAllSubmissions().map {
            val submission = serializationService.deserializeSubmission(it.body, SubFormat.JSON)
            logger.info { "Submitting ${submission.accNo}" }
        }
    }
}
