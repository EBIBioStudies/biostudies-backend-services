package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import uk.ac.ebi.biostd.extended.model.ExtSection
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.model.constants.ProcessingStatus
import uk.ac.ebi.biostd.model.constants.SectionFields
import uk.ac.ebi.biostd.model.extensions.releaseDate
import uk.ac.ebi.biostd.model.extensions.releaseTime
import uk.ac.ebi.biostd.model.extensions.title
import uk.ac.ebi.biostd.util.date.isBeforeOrEqual
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy:HH:mm")

fun DocSubmission.asBasicSubmission(status: ProcessingStatus): BasicSubmission =
    BasicSubmission(
        accNo = accNo,
        displayAccNo = accNo,
        newSubmission = false,
        title = section.title ?: title,
        released = released,
        modificationTime = modificationTime.atOffset(UTC).truncatedTo(ChronoUnit.MILLIS),
        releaseTime = releaseTime.atOffset(UTC).truncatedTo(ChronoUnit.MILLIS),
        status = status,
        owner = owner,
        errors = emptyList(),
    )

fun Submission.asSubmittedRequest(
    owner: String,
    creationTime: OffsetDateTime,
    newSubmission: Boolean,
): BasicSubmission =
    BasicSubmission(
        accNo = accNo,
        displayAccNo = if (newSubmission) creationTime.format(formatter) else accNo,
        newSubmission = newSubmission,
        title = section.title ?: title,
        released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now(UTC)) ?: false,
        modificationTime = OffsetDateTime.now(UTC),
        releaseTime =
            releaseDate?.let { LocalDate.parse(it).atStartOfDay().atOffset(UTC) }
                ?: OffsetDateTime.now(UTC),
        status = ProcessingStatus.PROCESSING,
        owner = owner,
        errors = emptyList(),
    )

fun ExtSubmission.asBasicSubmission(
    status: ProcessingStatus,
    newSubmission: Boolean,
    errors: List<String> = emptyList(),
): BasicSubmission =
    BasicSubmission(
        accNo = accNo,
        displayAccNo = if (newSubmission) creationTime.format(formatter) else accNo,
        newSubmission = newSubmission,
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
