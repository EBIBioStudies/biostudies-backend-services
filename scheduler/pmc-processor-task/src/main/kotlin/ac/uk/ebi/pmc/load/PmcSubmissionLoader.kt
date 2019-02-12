package ac.uk.ebi.pmc.load

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat.TSV
import ac.uk.ebi.pmc.persistence.MongoDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import arrow.core.Try
import ebi.ac.uk.base.splitIgnoringEmpty
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SUB_SEPARATOR
import ebi.ac.uk.model.extensions.getSectionByType
import ebi.ac.uk.model.extensions.releaseTime
import ebi.ac.uk.util.regex.getGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val SANITIZE_REGEX = "(\n)(\t)*|(\t)+(\n)".toRegex()
private const val PUB_SECTION = "Publication"
private const val PUB_DATE = "Publication date"
private var YEAR_PATTERN = "\\d{4}".toRegex()

class PmcSubmissionLoader(
    private val serializationService: SerializationService,
    private val mongoDocService: MongoDocService,
    private val submissionService: SubmissionDocService
) {

    /**
     * Process the given plain file and load submissions into database. Previously loaded submission are deprecated
     * when new version is found and any issue processing the file system is register in the errors collection.
     *
     * @param file submissions load file data including content and name.
     */
    suspend fun processFile(file: FileSpec) = withContext(Dispatchers.Default) {
        if (mongoDocService.isProcessed(file).not()) {
            sanitize(file.content)
                .splitIgnoringEmpty(SUB_SEPARATOR)
                .map { deserialize(it) }
                .map { (body, result) -> launch { processSubmission(result, body, file) } }

            mongoDocService.reportProcessed(file)
        }
    }

    private suspend fun processSubmission(result: Try<Submission>, body: String, file: FileSpec) =
        result.fold(
            { mongoDocService.saveError(file.name, body, it) },
            { loadSubmission(it, file.name, file.modified) })

    private fun deserialize(pagetab: String) =
        Pair(pagetab, Try { serializationService.deserializeSubmission(pagetab, TSV) })

    private suspend fun loadSubmission(submission: Submission, sourceFile: String, sourceTime: Instant) {
        submission.releaseTime = getReleaseDate(submission)
        submissionService.saveLoadedVersion(submission, sourceFile, sourceTime)
    }

    private fun getReleaseDate(submission: Submission): Instant {
        val releaseDate: String = submission.getSectionByType(PUB_SECTION)[PUB_DATE]
        val year = YEAR_PATTERN.getGroup(releaseDate).toInt()
        return LocalDate.ofYearDay(year, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    }

    private fun sanitize(fileText: String) = fileText.replace(SANITIZE_REGEX, "\n")
}
