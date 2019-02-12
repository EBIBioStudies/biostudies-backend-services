package ac.uk.ebi.pmc.persistence

import ac.uk.ebi.pmc.load.FileSpec
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.ERROR
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace

private val logger = KotlinLogging.logger {}

class MongoDocService(
    private val subRepository: SubmissionRepository,
    private val errorsRepository: ErrorsRepository,
    private val inputFileRepo: InputFileRepository
) {

    suspend fun isProcessed(file: FileSpec) = inputFileRepo.find(file).isDefined()

    suspend fun reportProcessed(file: FileSpec) = inputFileRepo.save(file)

    suspend fun saveError(submission: SubmissionDoc, throwable: Throwable) {
        logger.error { "Error processing submission ${submission.accNo} from file ${submission.sourceFile}, ${throwable.message}" }
        subRepository.update(submission.withStatus(ERROR))
        errorsRepository.save(SubmissionErrorDoc(submission, getStackTrace(throwable)))
    }

    suspend fun saveError(sourceFile: String, submissionBody: String, throwable: Throwable) {
        errorsRepository.save(SubmissionErrorDoc(sourceFile, submissionBody, getStackTrace(throwable)))
    }
}
