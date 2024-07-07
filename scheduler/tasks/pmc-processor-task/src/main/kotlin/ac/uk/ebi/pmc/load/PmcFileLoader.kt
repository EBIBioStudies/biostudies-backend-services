package ac.uk.ebi.pmc.load

import ebi.ac.uk.base.Either
import ebi.ac.uk.base.Either.Companion.left
import ebi.ac.uk.base.Either.Companion.right
import ebi.ac.uk.functions.milisToInstant
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FilenameFilter
import java.util.zip.GZIPInputStream

private val logger = KotlinLogging.logger {}

class PmcFileLoader(private val pmcLoader: PmcSubmissionLoader) {
    /**
     * List the files in the given folder and load into the system the ones not already loaded. Sequence is used so the
     * full list of file content is not loaded into memory.
     *
     * @param folder folder containing submission gzip files to be loaded into the system.
     * @param file optional file to load.
     */
    fun loadFile(
        folder: File,
        file: File?,
    ) {
        val files = if (file != null) listOf(file) else folder.listFiles(GzFilter).orEmpty().toList()
        logger.info { "loading files ${files.joinToString()}" }
        processFiles(
            toProcess = files,
            processedFolder = folder.createSubFolder("processed"),
            failedFolder = folder.createSubFolder("failed"),
        )
    }

    private fun processFiles(
        toProcess: List<File>,
        processedFolder: File,
        failedFolder: File,
    ) {
        toProcess.asSequence()
            .onEach { file -> logger.info { "checking file '${file.absolutePath}'" } }
            .map { file -> runCatching { getFileData(file) }.fold({ left(it) }, { right(Pair(file, it)) }) }
            .forEach { either ->
                runBlocking { processFile(either, processedFolder, failedFolder) }
            }
    }

    private suspend fun processFile(
        loadResult: Either<FileSpec, Pair<File, Throwable>>,
        processedFolder: File,
        failedFolder: File,
    ) {
        loadResult.fold(
            { pmcLoader.processFile(it, processedFolder) },
            { (file, error) -> pmcLoader.processCorruptedFile(file, failedFolder, error) },
        )
    }

    private fun getFileData(file: File): FileSpec {
        val entryContent = GZIPInputStream(FileInputStream(file)).use { IOUtils.toString(it, Charsets.UTF_8) }
        return FileSpec(file.absolutePath, entryContent, milisToInstant(file.lastModified()), file)
    }

    private fun File.createSubFolder(name: String): File {
        val folder = resolve(name)
        folder.mkdir()
        return folder
    }

    object GzFilter : FilenameFilter {
        override fun accept(
            dir: File,
            name: String,
        ): Boolean = name.lowercase().endsWith(".gz")
    }
}
