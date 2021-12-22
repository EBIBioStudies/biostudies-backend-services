package uk.ac.ebi.scheculer.pmc.exporter.service

import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTPClient
import uk.ac.ebi.scheculer.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheculer.pmc.exporter.mapping.toLink
import uk.ac.ebi.scheculer.pmc.exporter.model.Link
import uk.ac.ebi.scheculer.pmc.exporter.model.Links
import uk.ac.ebi.scheculer.pmc.exporter.model.PmcData
import uk.ac.ebi.scheculer.pmc.exporter.persistence.PmcRepository
import uk.ac.ebi.scheculer.pmc.exporter.persistence.pageIterator
import java.io.PrintWriter

internal const val CHUNK_SIZE = 4000
internal const val BUFFER_SIZE = 1024 * 1024

private val logger = KotlinLogging.logger {}

class PmcExporterService(
    private val pmcRepository: PmcRepository,
    private val appProperties: ApplicationProperties,
) {
    private val xmlWriter = xmlWriter()
    private val ftpClient = ftpClient()
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
        ftpClient.storeFile(path, xml.byteInputStream())
    }

    private fun ftpClient() = FTPClient().apply {
        bufferSize = BUFFER_SIZE
        addProtocolCommandListener(PrintCommandListener(PrintWriter(System.out)))
    }

    private fun FTPClient.login() {
        val ftpConfig = appProperties.ftp

        connect(ftpConfig.host, ftpConfig.port.toInt())
        login(ftpConfig.user, ftpConfig.password)
    }

    private fun xmlWriter(): XmlMapper =
        XmlMapper(
            JacksonXmlModule().apply { setDefaultUseWrapper(false) }
        ).apply { enable(INDENT_OUTPUT) }
}
