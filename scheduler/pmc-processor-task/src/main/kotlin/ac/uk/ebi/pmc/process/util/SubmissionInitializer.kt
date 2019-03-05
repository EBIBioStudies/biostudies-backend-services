package ac.uk.ebi.pmc.process.util

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.getSectionByType
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.util.regex.getGroup
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val PUBLICATION_SECTION = "Publication"
private const val PUBLICATION_DATE_ATTRIBUTE = "Publication date"

private val yearExtractionPattern = "\\d{4}".toRegex()

/**
 * Read submission value an set calculated values.
 */
class SubmissionInitializer(private val serializationService: SerializationService) {

    fun getSubmission(body: String): Submission {
        val submission = serializationService.deserializeSubmission(body, SubFormat.JSON)
        submission.releaseDate = getReleaseDate(submission)
        submission.accNo =
            return submission
    }

    private fun getReleaseDate(submission: Submission): Instant {
        val releaseDate: String = submission.getSectionByType(PUBLICATION_SECTION)[PUBLICATION_DATE_ATTRIBUTE]
        val year = yearExtractionPattern.getGroup(releaseDate).toInt()
        return LocalDate.ofYearDay(year, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    }
}
