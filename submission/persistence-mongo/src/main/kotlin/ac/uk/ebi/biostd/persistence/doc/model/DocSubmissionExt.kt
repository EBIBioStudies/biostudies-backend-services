package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.SectionFields
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

fun DocSubmission.asBasicSubmission(status: ProcessingStatus): BasicSubmission {
    return BasicSubmission(
        accNo = accNo,
        version = version,
        secretKey = secretKey,
        title = section.title ?: title,
        relPath = relPath,
        released = released,
        creationTime = creationTime.atOffset(UTC).truncatedTo(ChronoUnit.MILLIS),
        modificationTime = modificationTime.atOffset(UTC).truncatedTo(ChronoUnit.MILLIS),
        releaseTime = releaseTime?.atOffset(UTC)?.truncatedTo(ChronoUnit.MILLIS),
        status = status,
        method = method.toSubmissionMethod(),
        owner = owner,
    )
}

private fun DocSubmissionMethod.toSubmissionMethod(): SubmissionMethod =
    when (this) {
        DocSubmissionMethod.FILE -> SubmissionMethod.FILE
        DocSubmissionMethod.PAGE_TAB -> SubmissionMethod.PAGE_TAB
        DocSubmissionMethod.UNKNOWN -> SubmissionMethod.UNKNOWN
    }

fun ExtSubmission.asBasicSubmission(status: ProcessingStatus): BasicSubmission =
    BasicSubmission(
        accNo = this.accNo,
        version = version,
        secretKey = secretKey,
        title = section.title ?: title,
        relPath = relPath,
        released = released,
        creationTime = creationTime,
        modificationTime = modificationTime,
        releaseTime = releaseTime,
        status = status,
        method = method.toSubmissionMethod(),
        owner = owner,
    )

val ExtSection.title: String?
    get() = attributes.find { it.name == SectionFields.TITLE.value }?.value

private fun ExtSubmissionMethod.toSubmissionMethod(): SubmissionMethod =
    when (this) {
        ExtSubmissionMethod.FILE -> SubmissionMethod.FILE
        ExtSubmissionMethod.PAGE_TAB -> SubmissionMethod.PAGE_TAB
        ExtSubmissionMethod.UNKNOWN -> SubmissionMethod.UNKNOWN
    }
