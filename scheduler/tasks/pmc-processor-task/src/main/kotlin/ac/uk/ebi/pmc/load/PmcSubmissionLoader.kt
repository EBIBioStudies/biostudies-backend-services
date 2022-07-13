package ac.uk.ebi.pmc.load

import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.InputFilesDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.base.splitIgnoringEmpty
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SUB_SEPARATOR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File

private val sanitizeRegex = "(\n)(\t)*|(\t)+(\n)".toRegex()

private const val BUFFER_SIZE = 30
private val logger = KotlinLogging.logger {}

class PmcSubmissionLoader(
    private val pmcSubmissionTabProcessor: PmcSubmissionTabProcessor,
    private val errorDocService: ErrorsDocService,
    private val inputFilesDocService: InputFilesDocService,
    private val submissionService: SubmissionDocService,
) {
    /**
     * Process the given plain file and load submissions into database. Previously loaded submission are register
     * when new version is found and any issue processing the file is registered in the errors collection.
     *
     * @param file submissions load file data including content and name.
     */
    suspend fun processFile(file: FileSpec, processedFolder: File) = withContext(Dispatchers.Default) {
        logger.info { "processing file ${file.name}" }
        val toLoad = loadSubmissions(file)
        toLoad.buffer(BUFFER_SIZE).collect {
            val (source, submission, positionInFile) = it
            val (body, result) = deserialize(submission)
            loadSubmission(result, body, source, positionInFile)
        }

        inputFilesDocService.reportProcessed(file)
        moveFile(file.originalFile, processedFolder.resolve(file.originalFile.name))
    }

    suspend fun processCorruptedFile(pair: Pair<File, Throwable>, failedFolder: File) {
        logger.info { "processing file ${pair.first.name}" }
        inputFilesDocService.reportFailed(pair.first, pair.second.stackTraceToString())
        moveFile(pair.first, failedFolder.resolve(pair.first.name))
    }

    private fun moveFile(file: File, processed: File) {
        file.copyTo(processed, overwrite = true)
        file.delete()
    }

    private fun loadSubmissions(file: FileSpec): Flow<LoadedSubmissionInfo> = flow {
        sanitize(file.content)
            .splitIgnoringEmpty(SUB_SEPARATOR)
            .forEachIndexed { index, submissionBody -> emit(LoadedSubmissionInfo(file, submissionBody, index)) }
    }

    private suspend fun loadSubmission(result: Result<Submission>, body: String, file: FileSpec, positionInFile: Int) =
        result.fold(
            { submissionService.saveLoadedVersion(it, file.name, file.modified, positionInFile) },
            { errorDocService.saveError(file.name, body, PmcMode.LOAD, it) },
        )

    private fun deserialize(originalPagetab: String): Pair<String, Result<Submission>> =
        Pair(
            originalPagetab,
            runCatching { pmcSubmissionTabProcessor.transformSubmission(originalPagetab) }
        )

    private fun sanitize(fileText: String) = fileText.replace(sanitizeRegex, "\n")
}

private data class LoadedSubmissionInfo(
    val file: FileSpec,
    val body: String,
    val positionInFile: Int,
)
