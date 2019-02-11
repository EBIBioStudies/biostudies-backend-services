package ac.uk.ebi.pmc.persistence

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.load.FileSpec
import ac.uk.ebi.pmc.persistence.docs.SubStatus
import ac.uk.ebi.pmc.persistence.docs.SubStatus.ERROR
import ac.uk.ebi.pmc.persistence.docs.SubStatus.LOADED
import ac.uk.ebi.pmc.persistence.docs.SubStatus.PROCESED
import ac.uk.ebi.pmc.persistence.docs.SubStatus.PROCESSING
import ac.uk.ebi.pmc.persistence.docs.SubStatus.SUBMITTING
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubRepository
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import org.bson.types.ObjectId
import java.io.File
import java.time.Instant

private val logger = KotlinLogging.logger {}

class MongoDocService(
    private val subRepository: SubRepository,
    private val errorsRepository: ErrorsRepository,
    private val fileRepository: SubFileRepository,
    private val serializationService: SerializationService
) {

    suspend fun getReadyToProcess() = subRepository.findNext(LOADED, PROCESSING)

    suspend fun getReadyToSubmit() = subRepository.findNext(PROCESED, SUBMITTING)

    suspend fun getSubFiles(ids: List<ObjectId>) = fileRepository.getFiles(ids)

    suspend fun markAs(submission: SubmissionDoc, status: SubStatus) =
        subRepository.update(submission.withStatus(status))

    suspend fun expireOldVersions(submission: Submission, sourceFileTime: Instant) {
        subRepository.expireOldVersions(submission.accNo, sourceFileTime)
    }

    suspend fun saveNewVersion(submission: Submission, sourceFile: String) {
        subRepository.save(SubmissionDoc(submission.accNo, asJson(submission), sourceFile, LOADED))
        logger.info { "finish processing submission with accNo = '${submission.accNo}' from file $sourceFile" }
    }

    suspend fun saveSubmission(submission: Submission, sourceFile: String, files: List<File>) = coroutineScope {
        val fileIds = files
            .map { async { fileRepository.saveFile(it, submission.accNo) } }
            .awaitAll()

        subRepository.save(SubmissionDoc(submission.accNo, asJson(submission), sourceFile, LOADED, fileIds))
        logger.info { "finish processing submission with accNo = '${submission.accNo}' from file $sourceFile" }
    }

    fun isProcessed(file: FileSpec): Boolean {
        throw NotImplementedError() //TODO implemented loaded file repository
    }

    fun reportProcessed(file: FileSpec) {
        throw NotImplementedError() //TODO implemented loaded file repository
    }

    suspend fun saveError(submission: SubmissionDoc, throwable: Throwable) {
        logger.error { "Error processing submission ${submission.accNo} from file ${submission.sourceFile}, ${throwable.message}" }
        subRepository.update(submission.withStatus(ERROR))
        errorsRepository.save(SubmissionErrorDoc(submission, getStackTrace(throwable)))
    }

    suspend fun saveError(sourceFile: String, submissionBody: String, throwable: Throwable) {
        errorsRepository.save(SubmissionErrorDoc(sourceFile, submissionBody, getStackTrace(throwable)))
    }

    private fun asJson(submission: Submission) = serializationService.serializeSubmission(submission, SubFormat.JSON)
}
