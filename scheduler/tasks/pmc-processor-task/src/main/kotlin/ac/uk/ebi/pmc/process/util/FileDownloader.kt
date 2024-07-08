package ac.uk.ebi.pmc.process.util

import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.utils.retry
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

/**
 * In charge of download PMC files.
 */
class FileDownloader(
    private val properties: PmcImporterProperties,
    private val pmcApi: PmcApi,
) {
    suspend fun downloadFiles(submission: Submission): Result<List<File>> {
        return runCatching { donwloadSubFiles(submission) }
    }

    private suspend fun donwloadSubFiles(submission: Submission): List<File> {
        return submission
            .allFiles()
            .asFlow()
            .concurrently(5) { retry(times = 3) { downloadFile(getPmcId(submission.accNo), it) } }
            .toList()
    }

    private suspend fun downloadFile(
        pmcId: String,
        file: BioFile,
    ): File =
        withContext(Dispatchers.IO) {
            val targetFile = Paths.get(properties.temp).resolve(pmcId).resolve(file.path).toFile()
            targetFile.parentFile.apply { mkdirs() }

            val pmcFileStream = pmcApi.downloadFileStream(pmcId, file.path).byteStream()
            pmcFileStream.copyToFile(targetFile)
            return@withContext targetFile
        }

    private fun InputStream.copyToFile(destinationFile: File) {
        use { input -> destinationFile.outputStream().use { output -> input.copyTo(output) } }
    }

    private fun getPmcId(accNo: String) = accNo.removePrefix("S-EPMC")
}
