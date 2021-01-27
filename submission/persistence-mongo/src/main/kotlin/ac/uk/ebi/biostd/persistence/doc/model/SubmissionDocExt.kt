package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import java.time.ZoneOffset

fun DocSubmission.asBasicSubmission(): BasicSubmission {
    return BasicSubmission(
        accNo = accNo,
        version = version,
        secretKey = secretKey,
        title = title,
        relPath = relPath,
        released = released,
        creationTime = creationTime.atOffset(ZoneOffset.UTC),
        modificationTime = modificationTime.atOffset(ZoneOffset.UTC),
        releaseTime = releaseTime?.atOffset(ZoneOffset.UTC),
        status = status.toProcessingStatus(),
        method = method.toSubmissionMethod(),
        owner = owner)
}

private fun DocProcessingStatus.toProcessingStatus(): ProcessingStatus =
    when (this) {
        DocProcessingStatus.PROCESSED -> ProcessingStatus.PROCESSED
        DocProcessingStatus.PROCESSING -> ProcessingStatus.PROCESSING
        DocProcessingStatus.REQUESTED -> ProcessingStatus.REQUESTED
    }

private fun DocSubmissionMethod.toSubmissionMethod(): SubmissionMethod =
    when (this) {
        DocSubmissionMethod.FILE -> SubmissionMethod.FILE
        DocSubmissionMethod.PAGE_TAB -> SubmissionMethod.PAGE_TAB
        DocSubmissionMethod.UNKNOWN -> SubmissionMethod.UNKNOWN
    }
