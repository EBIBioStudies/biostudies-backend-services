package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.date.isBeforeOrEqual
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

open class SubmissionSubmitter(
    private val filesHandler: FilesHandler,
    private val timesService: TimesService,
    private val accNoService: AccNoService,
    private val persistenceContext: PersistenceContext
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(request: SubmissionRequest): Submission {
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
        val parentTags = persistenceContext.getParentAccessTags(submission).filterNot { it == "Public" }
        val (creationTime, modificationTime, releaseTime) = timesService.getTimes(submission)

        submission.accessTags = parentTags.toMutableList()
        if (releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()) {
            submission.released = true
            submission.addAccessTag(SubFields.PUBLIC_ACCESS_TAG.value)
        }

        val accNo = accNoService.getAccNo(submission)
        val accString = accNo.toString()
        val relPath = accNoService.getRelPath(accNo)
        val secretKey =
            if (persistenceContext.isNew(accString)) UUID.randomUUID().toString()
            else persistenceContext.getSecret(accString)

        submission.accNo = accString
        submission.relPath = relPath
        submission.releaseDate = null
        submission.creationTime = creationTime
        submission.modificationTime = modificationTime
        submission.releaseTime = releaseTime
        submission.secretKey = secretKey

        return submission
    }
}
