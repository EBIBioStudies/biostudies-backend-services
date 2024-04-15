package ac.uk.ebi.pmc.process.util

import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.utils.retry
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import ebi.ac.uk.model.BioFile as SubmissionFile

/**
 * In charge of download PMC files.
 */
class FileDownloader(
    private val properties: PmcImporterProperties,
    private val pmcApi: PmcApi,
) {
    suspend fun downloadFiles(submission: Submission): Result<List<File>> {
        return runCatching {
            coroutineScope {
                submission.allFiles()
                    .map { async { retry(times = 3) { downloadFile(getPmcId(submission.accNo), it) } } }
                    .awaitAll()
            }
        }
    }

    private suspend fun downloadFile(
        pmcId: String,
        file: SubmissionFile,
    ): File =
        withContext(Dispatchers.IO) {
            val targetFolder = Paths.get(properties.temp).resolve(pmcId).toFile()
            targetFolder.mkdirs()

            val targetFile = targetFolder.resolve(file.path)
            pmcApi.downloadFileStream(pmcId, file.path).byteStream().copyToFile(targetFile)
            return@withContext targetFile
        }

    private fun InputStream.copyToFile(destinationFile: File) {
        use { input -> destinationFile.outputStream().use { output -> input.copyTo(output) } }
    }

    private fun getPmcId(accNo: String) = accNo.removePrefix("S-EPMC")
}
