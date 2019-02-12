package ac.uk.ebi.pmc.load

import ebi.ac.uk.functions.milisToInstant
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

class PmcLoader(private val pmcLoader: PmcSubmissionLoader) {

    /**
     * List the files in the given folder and load into the system the ones not already loaded.
     *
     * @folder folder containing submission gzip files to be loaded into the system.
     */
    fun loadFolder(folder: File) {
        val files = folder.listFiles().map(::getFileData)
        runBlocking {
            files.forEach { pmcLoader.processFile(it) }
        }
    }

    private fun getFileData(file: File): FileSpec {
        val zipFile = GZIPInputStream(FileInputStream(file))
        val entryContent = IOUtils.toString(zipFile, Charsets.UTF_8)
        return FileSpec(file.absolutePath, entryContent, milisToInstant(file.lastModified()))
    }
}
