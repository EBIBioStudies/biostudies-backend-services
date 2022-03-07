package uk.ac.ebi.scheduler.exporter.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.io.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.net.ftp.FTPClient
import uk.ac.ebi.scheduler.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.exporter.mapping.toLink
import uk.ac.ebi.scheduler.exporter.model.Link
import uk.ac.ebi.scheduler.exporter.model.Links
import uk.ac.ebi.scheduler.exporter.model.PmcData
import uk.ac.ebi.scheduler.exporter.persistence.PmcRepository
import uk.ac.ebi.scheduler.exporter.persistence.pageIterator
import java.nio.file.Paths

internal const val CHUNK_SIZE = 4000

private val logger = KotlinLogging.logger {}

class PmcExporterService(
    pmcRepository: PmcRepository,
    private val xmlWriter: XmlMapper,
    private val ftpClient: FTPClient,
    private val appProperties: ApplicationProperties,
) {
    private val pmcDataIterator = pageIterator(pmcRepository)

    suspend fun exportPmcLinks() = withContext(Dispatchers.IO) {
        logger.info { "Started exporting PMC links" }

        ftpClient.login()

        pmcDataIterator
            .asSequence()
            .mapIndexed { part, page -> launch { writeLinks(part + 1, page.content) } }
            .toList()
            .joinAll()

        ftpClient.logout()
        ftpClient.disconnect()

        logger.info { "Finished exporting PMC links" }
    }

    private suspend fun writeLinks(part: Int, submissions: List<PmcData>) {
        logger.info { "Exporting PMC links part $part" }
        val links = submissions.map { it.toLink() }
        withContext(Dispatchers.IO) { write(part, links) }
    }

    private fun write(part: Int, links: List<Link>) {
        logger.info { "writing file part $part" }
        val xml = xmlWriter.writeValueAsString(Links(links))
        val path = "${appProperties.outputPath}/${String.format(appProperties.fileName, part)}"
        FileUtils.writeContent(Paths.get(path).toFile(), xml)
        ftpClient.storeFile(path, xml.byteInputStream())
    }

    private fun FTPClient.login() {
        val ftpConfig = appProperties.ftp

        connect(ftpConfig.host, ftpConfig.port.toInt())
        login(ftpConfig.user, ftpConfig.password)
    }
}
