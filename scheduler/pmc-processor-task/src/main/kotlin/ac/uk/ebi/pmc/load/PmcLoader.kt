package ac.uk.ebi.pmc.load

import ebi.ac.uk.functions.milisToInstant
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

private val logger = KotlinLogging.logger {}

class PmcLoader(private val pmcLoader: PmcSubmissionLoader) {

    /**
     * List the files in the given folder and load into the system the ones not already loaded. Sequence is used so the
     * full list of file content is not loaded into memory.
     *
     * @folder folder containing submission gzip files to be loaded into the system.
     */
    fun loadFolder(folder: File) {
        runBlocking {
            folder.listFiles()
                .asSequence()
                .filter { it.extension == "gz" }
                .onEach { logger.info { "checking file '${it.absolutePath}'" } }
                .map(::getFileData)
                .forEach { pmcLoader.processFile(it) }
        }
    }

    private fun getFileData(file: File): FileSpec {
        val zipFile = GZIPInputStream(FileInputStream(file))
        val entryContent = IOUtils.toString(zipFile, Charsets.UTF_8)
        return FileSpec(file.absolutePath, entryContent, milisToInstant(file.lastModified()))
    }
}
