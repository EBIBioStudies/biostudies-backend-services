package uk.ac.ebi.scheculer.pmc.exporter.service

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.extensions.getExtSubmissionsAsSequence
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.extended.model.ExtSubmission
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
import java.io.PrintWriter

internal const val CHUNK_SIZE = 4000
internal const val BUFFER_SIZE = 1024 * 1024
internal const val PMC_COLLECTION = "EuropePMC"

private val logger = KotlinLogging.logger {}

class PmcExporterService(
    private val bioWebClient: BioWebClient,
    private val appProperties: ApplicationProperties,
) {
    private val xmlWriter = xmlWriter()
    private val ftpClient = ftpClient()

    suspend fun exportPmcLinks() = withContext(Dispatchers.IO) {
        logger.info { "Started exporting PMC links" }

        ftpClient.login()

        bioWebClient
            .getExtSubmissionsAsSequence(ExtPageQuery(collection = PMC_COLLECTION))
            .chunked(CHUNK_SIZE)
            .mapIndexed { part, submissions -> launch { writeLinks(part, submissions) } }
            .toList()
            .joinAll()

        ftpClient.logout()
        ftpClient.disconnect()

        logger.info { "Finished exporting PMC links" }
    }

    private suspend fun writeLinks(part: Int, submissions: List<ExtSubmission>) {
        logger.info { "Exporting PMC links part $part" }
        val links = submissions.map { it.toLink() }
        withContext(Dispatchers.IO) { write(part, links) }
    }

    private fun write(part: Int, links: List<Link>) {
        val xml = xmlWriter.writeValueAsString(Links(links))
        val path = "${appProperties.outputPath}/${appProperties.fileName}part$part.xml"
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
