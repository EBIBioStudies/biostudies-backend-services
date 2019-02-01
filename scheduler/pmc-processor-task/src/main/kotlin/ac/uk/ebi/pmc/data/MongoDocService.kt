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
import org.litote.kmongo.coroutine.toList
import java.io.File

private val logger = KotlinLogging.logger {}

class MongoDocService(
    private val dataRepository: MongoRepository,
    private val serializationService: SerializationService
) {
    suspend fun getNotImportedSubmissions(sourceFile: String) =
            dataRepository.getNotImportedSubmissionsBySourceFile(sourceFile).toList()

    suspend fun getSubFiles(ids: List<ObjectId>) = dataRepository.getSubFiles(ids)

    suspend fun markAsImported(submission: SubmissionDoc) = dataRepository.update(submission.apply { imported = true })

    suspend fun saveSubmission(submission: Submission, sourceFile: String, files: List<File>) {
        val fileIds = files
            .map { GlobalScope.async { dataRepository.saveSubFile(it, submission.accNo) } }
            .awaitAll()

        dataRepository.save(SubmissionDoc(submission.accNo, asJson(submission), sourceFile, fileIds))
        logger.info { "finish processing submission with accNo = '${submission.accNo}' of file $sourceFile" }
    }

    suspend fun reportError(submission: Submission, sourceFile: String, throwable: Throwable) =
            saveError(submission.accNo, sourceFile, asJson(submission), throwable)

    suspend fun reportError(submission: SubmissionDoc, throwable: Throwable) =
            saveError(submission.id, submission.sourceFile, submission.body, throwable)

    private suspend fun saveError(accNo: String, sourceFile: String, submission: String, throwable: Throwable) {
        logger.error { "Error processing submission $accNo of file $sourceFile, ${throwable.message}" }
        dataRepository.save(ErrorDoc(accNo, submission, submission, getStackTrace(throwable)))
    }

    private fun asJson(submission: Submission) = serializationService.serializeSubmission(submission, SubFormat.JSON)
}
