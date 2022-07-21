package ac.uk.ebi.pmc.persistence

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.pmc.persistence.docs.FileDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.LOADED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSING
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.SUBMITTING
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import com.mongodb.client.result.UpdateResult
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import org.bson.types.ObjectId
import java.io.File
import java.time.Instant

private val logger = KotlinLogging.logger {}

class SubmissionDocService(
    private val submissionRepository: SubmissionRepository,
    private val fileRepository: SubFileRepository,
    private val serializationService: SerializationService,
) {

    fun findReadyToProcess(): Flow<SubmissionDoc> = flow {
        while (true) {
            emit(submissionRepository.findAndUpdate(LOADED, PROCESSING) ?: break)
        }
    }

    fun findReadyToSubmit(): Flow<SubmissionDoc> = flow {
        while (true) {
            emit(submissionRepository.findAndUpdate(PROCESSED, SUBMITTING) ?: break)
        }
    }

    suspend fun getSubFiles(ids: List<ObjectId>): List<FileDoc> = fileRepository.getFiles(ids)

    suspend fun changeStatus(submission: SubmissionDoc, status: SubmissionStatus): UpdateResult =
        submissionRepository.update(submission.withStatus(status))

    suspend fun saveLoadedVersion(submission: Submission, sourceFile: String, sourceTime: Instant, posInFile: Int) {
        val doc = SubmissionDoc(submission.accNo, asJson(submission), LOADED, sourceFile, posInFile, sourceTime)
        submissionRepository.insertOrExpire(doc)
        logger.info { "loaded version of submission with accNo = '${submission.accNo}' from file $sourceFile" }
    }

    suspend fun saveProcessedSubmission(doc: SubmissionDoc, files: List<File>) = coroutineScope {
        doc.files = saveFiles(files, doc)
        submissionRepository.update(doc.withStatus(PROCESSED))
        logger.info { "finish processing submission with accNo = '${doc.accNo}' from file ${doc.sourceFile}" }
    }

    private suspend fun saveFiles(files: List<File>, submission: SubmissionDoc): List<ObjectId> = coroutineScope {
        return@coroutineScope files
            .map { async { fileRepository.saveFile(it, submission.accNo) } }
            .awaitAll()
    }

    private fun asJson(submission: Submission) = serializationService.serializeSubmission(submission, SubFormat.JSON)
}
