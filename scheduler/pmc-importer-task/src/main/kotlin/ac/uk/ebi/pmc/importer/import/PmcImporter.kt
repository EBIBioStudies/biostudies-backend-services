package ac.uk.ebi.pmc.importer.import

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.pmc.importer.data.MongoDocService
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.getSectionByType
import ebi.ac.uk.model.extensions.releaseTime
import ebi.ac.uk.util.regex.getGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val SANITIZE_REGEX = "(\n)(\t)*|(\t)+(\n)".toRegex()
private const val PUB_SECTION = "Publication"
private const val PUB_DATE = "Publication date"
private var YEAR_PATTERN = "\\d{4}".toRegex()

private val logger = KotlinLogging.logger {}

class PmcImporter(
    private val submissionDocService: MongoDocService,
    private val serializationService: SerializationService,
    private val fileDownloader: FileDownloader
) {

    suspend fun processSubmissions(text: String, sourceFile: String): List<Job> {
        val submissions = serializationService.deserializeList(sanitize(text), SubFormat.TSV)
        return submissions.mapIndexed { index, submission ->
            GlobalScope.launch { processSubmission(index, submission, sourceFile) }
        }
    }

    private fun sanitize(fileText: String) = fileText.replace(SANITIZE_REGEX, "\n")

    private suspend fun processSubmission(index: Int, submission: Submission, sourceFile: String): Submission {
        logger.info { "processing submission $index with accNo = '${submission.accNo}' of file $sourceFile" }
        submission.releaseTime = getReleaseDate(submission)
        fileDownloader.downloadFiles(submission).fold(
            { submissionDocService.reportError(submission, sourceFile, it) },
            { submissionDocService.saveSubmission(submission, sourceFile, it) })
        return submission
    }

    private fun getReleaseDate(submission: Submission): Instant {
        val releaseDate: String = submission.getSectionByType(PUB_SECTION)[PUB_DATE]
        val year = YEAR_PATTERN.getGroup(releaseDate).toInt()
        return LocalDate.ofYearDay(year, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    }
}
