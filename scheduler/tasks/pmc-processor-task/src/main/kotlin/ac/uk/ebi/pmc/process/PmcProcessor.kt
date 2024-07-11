package ac.uk.ebi.pmc.process

import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.domain.ErrorsService
import ac.uk.ebi.pmc.persistence.domain.SubmissionService
import ac.uk.ebi.pmc.process.util.FileDownloader
import ac.uk.ebi.pmc.process.util.SubmissionInitializer
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.model.Submission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Paths

class PmcProcessor(
    private val submissionInitializer: SubmissionInitializer,
    private val errorService: ErrorsService,
    private val submissionService: SubmissionService,
    private val fileDownloader: FileDownloader,
    private val properties: PmcImporterProperties,
) {
    fun processAll(sourceFile: String?): Unit = runBlocking { processSubmissions(sourceFile) }

    private suspend fun processSubmissions(sourceFile: String?) {
        supervisorScope {
            submissionService.findReadyToProcess(sourceFile)
                .concurrently(CONCURRENCY) { processSubmission(it) }
                .collect()
        }
    }

    private suspend fun processSubmission(subDoc: SubmissionDocument) {
        runCatching { submissionInitializer.getSubmission(subDoc.body) }
            .fold(
                { (sub, subBody) -> downloadFiles(sub, subBody, subDoc) },
                { errorService.saveError(subDoc, PmcMode.PROCESS, it) },
            )
    }

    private suspend fun downloadFiles(
        sub: Submission,
        subBody: String,
        subDoc: SubmissionDocument,
    ) {
        val targetFolder = createTargetFolder(subDoc)
        fileDownloader.downloadFiles(targetFolder, sub).fold(
            { submissionService.saveProcessedSubmission(subDoc.copy(body = subBody), it) },
            { errorService.saveError(subDoc, PmcMode.PROCESS, it) },
        )
    }

    private suspend fun createTargetFolder(subDoc: SubmissionDocument): File =
        withContext(Dispatchers.IO) {
            val target =
                Paths.get(properties.temp)
                    .resolve(subDoc.accNo.takeLast(PARTITION_CHARACTERS))
                    .resolve(subDoc.accNo)
                    .resolve("${subDoc.sourceTime}")
                    .resolve("${subDoc.posInFile}")
                    .toFile()
            target.mkdirs()
            return@withContext target
        }

    companion object {
        private const val CONCURRENCY = 30
        private const val PARTITION_CHARACTERS = 3
    }
}
