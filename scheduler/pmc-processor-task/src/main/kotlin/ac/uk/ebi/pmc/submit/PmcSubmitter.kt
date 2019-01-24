package ac.uk.ebi.pmc.submit

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.data.MongoDocService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.forEach

private val logger = KotlinLogging.logger {}

class PmcSubmitter(
    private val submissionDocService: MongoDocService,
    private val serializationService: SerializationService
) {
    suspend fun submit() = coroutineScope {
        submissionDocService.getAllSubmissions().forEach {
            val submission = serializationService.deserializeSubmission(it.body.replace("\\", ""), SubFormat.JSON)
            logger.info { "to submit ${submission.accNo}" }
            async {
                uploadFiles(it.files)
                logger.info { "perform submission" }
            }
        }
    }

    private suspend fun uploadFiles(filesIds: List<ObjectId>) {
        submissionDocService.getSubFiles(filesIds).forEach {
            it.fold({ logger.info { "throw exception" } }, { logger.info { "upload ${ it.name }" } })
        }
    }
}
