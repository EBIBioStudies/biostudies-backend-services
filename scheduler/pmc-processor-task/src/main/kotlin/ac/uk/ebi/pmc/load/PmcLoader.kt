package ac.uk.ebi.pmc.load

import ebi.ac.uk.functions.milisToInstant
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FilenameFilter
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
            processFiles(toProcess = folder, processed = folder.createSubFolder("processed"))
        }
    }

    private fun File.createSubFolder(name: String): File {
        val folder = resolve(name)
        folder.mkdir()
        return folder
    }

    private suspend fun processFiles(toProcess: File, processed: File) {
        logger.info { "loading files in ${toProcess.absolutePath}" }
        toProcess.listFiles(GzFilter)
            .orEmpty()
            .asSequence()
            .onEach { logger.info { "checking file '${it.absolutePath}'" } }
            .map(::getFileData)
            .forEach { pmcLoader.processFile(it, processed) }
    }

    private fun getFileData(file: File): FileSpec {
        val entryContent = GZIPInputStream(FileInputStream(file)).use { IOUtils.toString(it, Charsets.UTF_8) }
        return FileSpec(file.absolutePath, entryContent, milisToInstant(file.lastModified()), file)
    }

    object GzFilter : FilenameFilter {
        override fun accept(dir: File, name: String): Boolean = name.toLowerCase().endsWith(".gz")
    }
}
