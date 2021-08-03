package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import com.google.common.collect.ImmutableList
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.SectionFields
import java.time.ZoneOffset

fun DocSubmission.asBasicSubmission(): BasicSubmission {
    return BasicSubmission(
        accNo = accNo,
        version = version,
        secretKey = secretKey,
        title = title ?: section.title,
        relPath = relPath,
        released = released,
        creationTime = creationTime.atOffset(ZoneOffset.UTC),
        modificationTime = modificationTime.atOffset(ZoneOffset.UTC),
        releaseTime = releaseTime?.atOffset(ZoneOffset.UTC),
        status = status.toProcessingStatus(),
        method = method.toSubmissionMethod(),
        owner = owner
    )
}

val DocSubmission.allDocSections: List<DocSection>
    get() = ImmutableList.builder<DocSection>()
        .add(section)
        .addAll(section.allSections)
        .build()
        .filterNotNull()

val DocSection.allSections: List<DocSection>
    get() = sections.flatMap { either -> either.fold({ it.allSections }, { emptyList() }) }

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

fun ExtSubmission.asBasicSubmission(): BasicSubmission = BasicSubmission(
    accNo = this.accNo,
    version = version,
    secretKey = secretKey,
    title = title ?: section.title,
    relPath = relPath,
    released = released,
    creationTime = creationTime,
    modificationTime = modificationTime,
    releaseTime = releaseTime,
    status = status.toProcessingStatus(),
    method = method.toSubmissionMethod(),
    owner = owner
)

val ExtSection.title: String?
    get() = attributes.find { it.name == SectionFields.TITLE.value }?.value

private fun ExtProcessingStatus.toProcessingStatus(): ProcessingStatus =
    when (this) {
        ExtProcessingStatus.PROCESSED -> ProcessingStatus.PROCESSED
        ExtProcessingStatus.PROCESSING -> ProcessingStatus.PROCESSING
        ExtProcessingStatus.REQUESTED -> ProcessingStatus.REQUESTED
    }

private fun ExtSubmissionMethod.toSubmissionMethod(): SubmissionMethod =
    when (this) {
        ExtSubmissionMethod.FILE -> SubmissionMethod.FILE
        ExtSubmissionMethod.PAGE_TAB -> SubmissionMethod.PAGE_TAB
        ExtSubmissionMethod.UNKNOWN -> SubmissionMethod.UNKNOWN
    }
