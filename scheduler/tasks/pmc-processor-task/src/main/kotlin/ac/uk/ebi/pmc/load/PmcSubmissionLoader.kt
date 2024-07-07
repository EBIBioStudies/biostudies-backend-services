package ac.uk.ebi.pmc.load

import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.InputFilesService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.base.splitIgnoringEmpty
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SUB_SEPARATOR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File

private val sanitizeRegex = "(\n)(\t)*|(\t)+(\n)".toRegex()

private const val CONCURRENCY = 30
private val logger = KotlinLogging.logger {}

class PmcSubmissionLoader(
    private val pmcSubmissionTabProcessor: PmcSubmissionTabProcessor,
    private val errorService: ErrorsService,
    private val inputFilesService: InputFilesService,
    private val submissionService: SubmissionService,
) {
    /**
     * Process the given plain file and load submissions into database. Previously loaded submission are register
     * when new version is found and any issue processing the file is registered in the errors collection.
     *
     * @param file submissions load file data including content and name.
     */
    suspend fun processFile(
        file: FileSpec,
        processedFolder: File,
    ) {
        logger.info { "processing file ${file.name} in folder ${processedFolder.name}" }

        loadSubmissions(file)
            .concurrently(CONCURRENCY, {
                val (source, submission, positionInFile) = it
                val (body, result) = deserialize(submission)
                loadSubmission(result, body, source, positionInFile)
            }).collect()

        inputFilesService.reportProcessed(file)
        moveFile(file.originalFile, processedFolder.resolve(file.originalFile.name))
    }

    suspend fun processCorruptedFile(
        file: File,
        failFolder: File,
        error: Throwable,
    ) {
        logger.info { "processing file ${file.name}" }
        inputFilesService.reportFailed(file, error.stackTraceToString())
        moveFile(file, failFolder.resolve(file.name))
    }

    private suspend fun moveFile(
        file: File,
        processed: File,
    ): Unit =
        withContext(Dispatchers.IO) {
            file.copyTo(processed, overwrite = true)
            file.delete()
        }

    private fun loadSubmissions(file: FileSpec): Flow<LoadedSubmissionInfo> {
        return file.content.replace(sanitizeRegex, "\n")
            .splitIgnoringEmpty(SUB_SEPARATOR)
            .mapIndexed { index, submissionBody -> LoadedSubmissionInfo(file, submissionBody, index) }
            .asFlow()
    }

    private suspend fun loadSubmission(
        result: Result<Submission>,
        body: String,
        file: FileSpec,
        positionInFile: Int,
    ): Unit =
        result.fold(
            { submissionService.saveLoadedVersion(it, file.name, positionInFile) },
            { errorService.saveError(file.name, body, PmcMode.LOAD, it) },
        )

    private fun deserialize(originalPagetab: String): Pair<String, Result<Submission>> =
        Pair(
            originalPagetab,
            runCatching { pmcSubmissionTabProcessor.transformSubmission(originalPagetab) },
        )
}

private data class LoadedSubmissionInfo(
    val file: FileSpec,
    val body: String,
    val positionInFile: Int,
)
