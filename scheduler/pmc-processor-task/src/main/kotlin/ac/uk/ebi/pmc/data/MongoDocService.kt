package ac.uk.ebi.pmc.data

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.data.docs.ErrorDoc
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import org.bson.types.ObjectId
import java.io.File

private val logger = KotlinLogging.logger {}

class MongoDocService(
    private val dataRepository: MongoRepository,
    private val serializationService: SerializationService
) {
    suspend fun getAllSubmissions() = dataRepository.getAllSubmissions()

    suspend fun getSubFiles(ids: List<ObjectId>) = dataRepository.getSubFiles(ids)

    suspend fun saveSubmission(submission: Submission, sourceFile: String, files: List<File>) {
        val fileIds = files
            .map { GlobalScope.async { dataRepository.saveSubFile(it, submission.accNo) } }
            .awaitAll()

        dataRepository.save(SubmissionDoc(submission.accNo, asJson(submission), sourceFile, fileIds))
        logger.info { "finish processing submission with accNo = '${submission.accNo}' of file $sourceFile" }
    }

    suspend fun reportError(submission: Submission, sourceFile: String, throwable: Throwable) {
        logger.error { "problem processing submission ${submission.accNo} of file $sourceFile, ${throwable.message}" }

        dataRepository.save(ErrorDoc(submission.accNo, asJson(submission), sourceFile, getStackTrace(throwable)))
    }

    private fun asJson(submission: Submission) = serializationService.serializeSubmission(submission, SubFormat.JSON)
}
