package ac.uk.ebi.pmc.process.util

import ac.uk.ebi.pmc.client.PmcApi
import ac.uk.ebi.pmc.utils.retry
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.io.File

/**
 * In charge of download PMC files.
 */
class FileDownloader(
    private val pmcApi: PmcApi,
) {
    suspend fun downloadFiles(
        targetFolder: File,
        submission: Submission,
    ): Result<List<File>> {
        return runCatching { donwloadSubFiles(targetFolder, submission) }
    }

    private suspend fun donwloadSubFiles(
        targetFolder: File,
        submission: Submission,
    ): List<File> {
        return submission
            .allFiles()
            .asFlow()
            .concurrently(CONCURRENCY) { retry(RETRIES) { downloadFile(targetFolder, getPmcId(submission.accNo), it) } }
            .toList()
    }

    private suspend fun downloadFile(
        targetFolder: File,
        pmcId: String,
        file: BioFile,
    ): File =
        withContext(Dispatchers.IO) {
            val targetFile = targetFolder.resolve(file.path)
            when (targetFile.exists()) {
                true -> targetFile
                false -> copyToFile(pmcApi.downloadFileStream(pmcId, file.path), targetFile)
            }
        }

    @Suppress("TooGenericExceptionCaught")
    private fun copyToFile(
        responseBody: okhttp3.ResponseBody,
        targetFile: File,
    ): File {
        val source = responseBody.byteStream()
        val target = targetFile.outputStream()

        try {
            source.use { input -> target.use { output -> input.copyTo(output) } }
        } catch (exception: Exception) {
            if (targetFile.exists()) targetFile.delete()
            throw exception
        }

        return targetFile
    }

    private fun getPmcId(accNo: String) = accNo.removePrefix("S-EPMC")

    companion object {
        const val CONCURRENCY = 5
        const val RETRIES = 3
    }
}
