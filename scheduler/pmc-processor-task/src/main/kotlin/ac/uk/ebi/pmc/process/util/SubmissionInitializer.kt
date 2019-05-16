package ac.uk.ebi.pmc.process.util

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.getSectionByType
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.util.regex.getGroup
import java.time.LocalDate

private const val PUBLICATION_SECTION = "Publication"
private const val PUBLICATION_DATE_ATTRIBUTE = "Publication date"

private val yearExtractionPattern = "\\d{4}".toRegex()

/**
 * Read submission value an set calculated values.
 */
class SubmissionInitializer(private val serializationService: SerializationService) {

    fun getSubmission(body: String): Pair<Submission, String> {
        val submission = serializationService.deserializeSubmission(body, SubFormat.JSON)
        submission.releaseDate = getReleaseDate(submission)
        return Pair(submission, asString(submission))
    }

    private fun asString(submission: Submission) =
        serializationService.serializeSubmission(submission, SubFormat.JSON)

    private fun getReleaseDate(submission: Submission): String {
        val releaseDate: String = submission.getSectionByType(PUBLICATION_SECTION)[PUBLICATION_DATE_ATTRIBUTE]
        val year = yearExtractionPattern.getGroup(releaseDate).toInt()
        return LocalDate.ofYearDay(year, 1).toString()
    }
}
