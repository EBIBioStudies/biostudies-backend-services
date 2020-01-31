package ac.uk.ebi.biostd.submission.processing

import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.util.date.isBeforeOrEqual
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

open class SubmissionSubmitService(
    private val filesHandler: FilesHandler,
    val context: PersistenceContext,
    val userPrivilegesService: IUserPrivilegesService,
    val patternUtil: AccNoPatternUtil
) {

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(request: SubmissionRequest, persistenceContext: PersistenceContext): Submission {
        val submission = ExtendedSubmission(request.submission, request.user.asUser())
        val processingErrors = process(submission, request.files)

        if (processingErrors.isEmpty()) {
            persistenceContext.deleteSubmissionDrafts(submission)
            submission.processingStatus = ProcessingStatus.PROCESSED
            submission.method = request.method
            return persistenceContext.saveSubmission(submission)
        }

        throw InvalidSubmissionException("Submission validation errors", processingErrors)
    }

    private fun process(submission: ExtendedSubmission, source: FilesSource) =
        listOf(
            runCatching { processSubmission(submission) },
            runCatching { filesHandler.processFiles(submission, source) }
        ).mapNotNull { it.exceptionOrNull() }

    private fun processSubmission(submission: ExtendedSubmission): ExtendedSubmission {
        val accNo = getAccNo(submission)
        val accString = accNo.toString()
        val relPath = getRelPath(accNo)
        val (creationTime, modificationTime, releaseTime) = getTimes(submission)
        val parentTags = parentTags(submission)
        val secretKey = if (context.isNew(accString)) UUID.randomUUID().toString() else context.getSecret(accString)
        submission.accNo = accString
        submission.relPath = relPath
        submission.creationTime = creationTime
        submission.modificationTime = modificationTime
        submission.releaseTime = releaseTime
        submission.accessTags = parentTags.toMutableList()
        submission.secretKey = secretKey

        if (releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()) {
            submission.released = true
            submission.addAccessTag(SubFields.PUBLIC_ACCESS_TAG.value)
        }

        return submission
    }
}

data class Test(val asd: String, val b: String)
