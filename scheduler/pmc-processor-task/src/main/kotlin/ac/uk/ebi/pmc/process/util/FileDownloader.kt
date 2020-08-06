package ac.uk.ebi.pmc.process.util

import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.utils.retry
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import arrow.core.Try
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Paths
import ebi.ac.uk.model.File as SubmissionFile

/**
 * In charge of download PMC files.
 */
class FileDownloader(
    private val properties: PmcImporterProperties,
    private val pmcApi: PmcApi
) {

    suspend fun downloadFiles(submission: Submission): Try<List<File>> {
        return Try {
            return@Try coroutineScope {
                submission.allFiles()
                    .map { async { retry(times = 3) { downloadFile(getPmcId(submission.accNo), it) } } }
                    .awaitAll()
            }
        }
    }

    private suspend fun downloadFile(pmcId: String, file: SubmissionFile): File = withContext(Dispatchers.IO) {
        val targetFolder = Paths.get(properties.temp).resolve(pmcId).toFile()
        targetFolder.mkdirs()

        val targetFile = targetFolder.resolve(file.path)
        FileUtils.copyInputStreamToFile(pmcApi.downloadFileAsync(pmcId, file.path).byteStream(), targetFile)
        return@withContext targetFile
    }

    private fun getPmcId(accNo: String) = accNo.removePrefix("S-EPMC")
}
