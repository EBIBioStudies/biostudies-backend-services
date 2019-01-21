package ac.uk.ebi.pmc.import

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class BatchPmcImporter(private val pmcImporter: PmcImporter) {

    fun importGzipFile(file: File) {
        runBlocking {
            val zipFile = GZIPInputStream(FileInputStream(file))
            val entryContent = IOUtils.toString(zipFile, Charsets.UTF_8)
            pmcImporter.processSubmissions(entryContent, file.name).joinAll()
        }
    }

    fun importFile(file: File) {
        val timeElapsed = measureTimeMillis {
            runBlocking { pmcImporter.processSubmissions(FileUtils.readFileToString(file, Charsets.UTF_8), file.name).joinAll() }
        }

        logger.info { "processed file ${file.name} in ${timeElapsed / 1000} seconds " }
    }
}
