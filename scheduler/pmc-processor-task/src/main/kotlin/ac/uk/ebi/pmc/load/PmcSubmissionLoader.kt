package ac.uk.ebi.pmc.load

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat.TSV
import ac.uk.ebi.pmc.persistence.ErrorsDocService
import ac.uk.ebi.pmc.persistence.InputFilesDocService
import ac.uk.ebi.pmc.persistence.SubmissionDocService
import ac.uk.ebi.scheduler.properties.PmcMode
import arrow.core.Try
import ebi.ac.uk.base.splitIgnoringEmpty
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SUB_SEPARATOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val sanitizeRegex = "(\n)(\t)*|(\t)+(\n)".toRegex()
private const val WORKERS = 30

class PmcSubmissionLoader(
    private val serializationService: SerializationService,
    private val errorDocService: ErrorsDocService,
    private val inputFilesDocService: InputFilesDocService,
    private val submissionService: SubmissionDocService
) {

    /**
     * Process the given plain file and load submissions into database. Previously loaded submission are deprecated
     * when new version is found and any issue processing the file is registered in the errors collection.
     *
     * @param file submissions load file data including content and name.
     */
    suspend fun processFile(file: FileSpec) = withContext(Dispatchers.Default) {
        if (inputFilesDocService.isProcessed(file).not()) {
            val receiveChannel = launchProducer(file)
            (1..WORKERS).map { launchProcessor(receiveChannel) }.joinAll()
            inputFilesDocService.reportProcessed(file)
        }
    }

    private fun CoroutineScope.launchProducer(file: FileSpec) = produce {
        sanitize(file.content)
            .splitIgnoringEmpty(SUB_SEPARATOR)
            .forEach { send(Pair(file, it)) }
        close()
    }

    private fun CoroutineScope.launchProcessor(channel: ReceiveChannel<Pair<FileSpec, String>>) = launch {
        for ((source, submission) in channel) {
            val (body, result) = deserialize(submission)
            loadSubmission(result, body, source)
        }
    }

    private suspend fun loadSubmission(result: Try<Submission>, body: String, file: FileSpec) =
        result.fold(
            { errorDocService.saveError(file.name, body, PmcMode.LOAD, it) },
            { submissionService.saveLoadedVersion(it, file.name, file.modified) })

    private fun deserialize(pagetab: String) =
        Pair(pagetab, Try { serializationService.deserializeSubmission(pagetab, TSV) })

    private fun sanitize(fileText: String) = fileText.replace(sanitizeRegex, "\n")
}
