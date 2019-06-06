package ac.uk.ebi.pmc.persistence

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.common.coroutines.SuspendSequence
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.LOADED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSING
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.SUBMITTING
import ac.uk.ebi.pmc.persistence.repository.SubFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.bson.types.ObjectId
import java.io.File
import java.time.Instant

private val logger = KotlinLogging.logger {}

class SubmissionDocService(
    private val submissionRepository: SubmissionRepository,
    private val fileRepository: SubFileRepository,
    private val serializationService: SerializationService
) {

    suspend fun findReadyToProcess() =
        SuspendSequence { submissionRepository.findAndUpdate(LOADED, PROCESSING) }

    suspend fun findReadyToSubmit() =
        SuspendSequence { submissionRepository.findAndUpdate(PROCESSED, SUBMITTING) }

    suspend fun getSubFiles(ids: List<ObjectId>) = fileRepository.getFiles(ids)

    suspend fun changeStatus(submission: SubmissionDoc, status: SubmissionStatus) =
        submissionRepository.update(submission.withStatus(status))

    suspend fun saveLoadedVersion(submission: Submission, sourceFile: String, sourceTime: Instant, posInFile: Int) {
        submissionRepository.insertOrExpire(SubmissionDoc(
            submission.accNo,
            asJson(submission),
            LOADED,
            sourceFile,
            posInFile,
            sourceTime))

        logger.info { "loaded version of submission with accNo = '${submission.accNo}' from file $sourceFile" }
    }

    suspend fun saveProcessedSubmission(submission: SubmissionDoc, files: List<File>) = coroutineScope {
        submission.files = saveFiles(files, submission)
        submissionRepository.update(submission.withStatus(PROCESSED))
        logger.info {
            "finish processing submission with accNo = '${submission.accno}' from file ${submission.sourceFile}"
        }
    }

    private suspend fun saveFiles(files: List<File>, submission: SubmissionDoc): List<ObjectId> = coroutineScope {
        return@coroutineScope files
            .map { async { fileRepository.saveFile(it, submission.accno) } }
            .awaitAll()
    }

    private fun asJson(submission: Submission) = serializationService.serializeSubmission(submission, SubFormat.JSON)
}
