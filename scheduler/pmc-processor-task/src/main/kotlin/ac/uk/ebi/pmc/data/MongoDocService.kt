package ac.uk.ebi.pmc.data

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.data.docs.ErrorDoc
import ac.uk.ebi.pmc.data.docs.SubStatus
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import ac.uk.ebi.pmc.data.repository.ErrorsRepository
import ac.uk.ebi.pmc.data.repository.SubFileRepository
import ac.uk.ebi.pmc.data.repository.SubRepository
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

    suspend fun getReadyToProcess() = subRepository.findNext(SubStatus.LOADED, SubStatus.DOWNLOADING)

    suspend fun getReadyToSubmit() = subRepository.findNext(SubStatus.DOWNLOADED, SubStatus.SUBMITTING)

    suspend fun getSubFiles(ids: List<ObjectId>) = fileRepository.getFiles(ids)

    suspend fun markAs(submission: SubmissionDoc, status: SubStatus) {
        subRepository.update(submission.apply { submissionStatus = status; updated = Instant.now() })
    }

    suspend fun saveSubmission(submission: Submission, sourceFile: String, files: List<File>) = coroutineScope {
        val fileIds = files
            .map { async { fileRepository.saveFile(it, submission.accNo) } }
            .awaitAll()

        subRepository.save(
            SubmissionDoc(
                submission.accNo,
                asJson(submission),
                sourceFile,
                fileIds,
                SubStatus.LOADED,
                Instant.now()))
        logger.info { "finish processing submission with accNo = '${submission.accNo}' from file $sourceFile" }
    }

    suspend fun saveError(submission: SubmissionDoc, throwable: Throwable) {
        logger.error { "Error processing submission ${submission.id} from file ${submission.sourceFile}, ${throwable.message}" }
        subRepository.update(submission.apply { submissionStatus = SubStatus.ERROR; updated = Instant.now() })
        errorsRepository.save(ErrorDoc(submission, getStackTrace(throwable)))
    }

    private fun asJson(submission: Submission) = serializationService.serializeSubmission(submission, SubFormat.JSON)

}
