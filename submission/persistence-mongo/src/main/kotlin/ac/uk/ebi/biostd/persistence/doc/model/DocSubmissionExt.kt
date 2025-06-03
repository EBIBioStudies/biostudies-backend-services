package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.SectionFields
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.releaseTime
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.date.isBeforeOrEqual
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

fun DocSubmission.asBasicSubmission(status: ProcessingStatus): BasicSubmission =
    BasicSubmission(
        accNo = accNo,
        title = section.title ?: title,
        released = released,
        modificationTime = modificationTime.atOffset(UTC).truncatedTo(ChronoUnit.MILLIS),
        releaseTime = releaseTime?.atOffset(UTC)?.truncatedTo(ChronoUnit.MILLIS),
        status = status,
        owner = owner,
        errors = emptyList(),
    )

fun Submission.asSubmittedRequest(owner: String): BasicSubmission =
    BasicSubmission(
        accNo = accNo,
        title = section.title ?: title,
        released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now(UTC)) ?: false,
        modificationTime = OffsetDateTime.now(UTC),
        releaseTime = releaseDate?.let { LocalDate.parse(it).atStartOfDay().atOffset(UTC) },
        status = ProcessingStatus.PROCESSING,
        owner = owner,
        errors = emptyList(),
    )

fun ExtSubmission.asBasicSubmission(
    status: ProcessingStatus,
    errors: List<String> = emptyList(),
): BasicSubmission =
    BasicSubmission(
        accNo = accNo,
        title = section.title ?: title,
        released = released,
        modificationTime = modificationTime,
        releaseTime = releaseTime,
        status = status,
        owner = owner,
        errors = errors,
    )

val ExtSection.title: String?
    get() = attributes.find { it.name == SectionFields.TITLE.value }?.value
