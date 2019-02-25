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

private const val PUB_SECTION = "Publication"
private const val PUB_DATE = "Publication date"
private var YEAR_PATTERN = "\\d{4}".toRegex()

/**
 * Read submission value an set calculated values.
 */
class SubmissionInitializer(private val serializationService: SerializationService) {

    fun getSubmission(body: String): Submission {
        val submission = serializationService.deserializeSubmission(body, SubFormat.JSON)
        submission.releaseDate = getReleaseDate(submission)
        return submission
    }

    private fun getReleaseDate(submission: Submission): Instant {
        val releaseDate: String = submission.getSectionByType(PUB_SECTION)[PUB_DATE]
        val year = YEAR_PATTERN.getGroup(releaseDate).toInt()
        return LocalDate.ofYearDay(year, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    }
}
