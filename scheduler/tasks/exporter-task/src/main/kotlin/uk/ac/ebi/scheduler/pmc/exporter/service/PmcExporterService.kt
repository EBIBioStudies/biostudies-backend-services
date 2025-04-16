package uk.ac.ebi.scheduler.pmc.exporter.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.coroutines.SuspendRetryTemplate
import ebi.ac.uk.coroutines.chunked
import ebi.ac.uk.coroutines.concurrently
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.scheduler.pmc.exporter.cli.BioStudiesFtpClient
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.mapping.toLink
import uk.ac.ebi.scheduler.pmc.exporter.model.Link
import uk.ac.ebi.scheduler.pmc.exporter.model.Links
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcRepository
import java.util.concurrent.atomic.AtomicInteger

internal const val CHUNK_SIZE = 4000

private val logger = KotlinLogging.logger {}

class PmcExporterService(
    private val pmcRepository: PmcRepository,
    private val xmlWriter: XmlMapper,
    private val appProperties: ApplicationProperties,
    private val retryTemplate: SuspendRetryTemplate,
) {
    suspend fun exportPmcLinks() {
        logger.info { "Started exporting PMC links" }
        val totalLinks = AtomicInteger(0)
        val parts = AtomicInteger(0)

        pmcRepository
            .findAllPmc()
            .chunked(CHUNK_SIZE)
            .buffer(20)
            .concurrently(10) {
                totalLinks.addAndGet(it.size)
                writeLinks(parts.incrementAndGet(), it)
                logger.info { "Exported '${totalLinks.get()}' links" }
            }.collect()

        logger.info { "Finished exporting PMC links. Total links: ${totalLinks.get()}" }
    }

    private suspend fun writeLinks(
        part: Int,
        submissions: List<PmcData>,
    ) {
        logger.info { "Exporting PMC links part $part" }
        val links = submissions.map { it.toLink() }
        write(part, links)
    }

    private suspend fun write(
        part: Int,
        links: List<Link>,
    ) = withContext(Dispatchers.IO) {
        val xml = xmlWriter.writeValueAsString(Links(links))
        val path = "${appProperties.outputPath}/${String.format(appProperties.fileName, part)}"

        retryTemplate.execute("storing file $path") {
            val ftpClient = BioStudiesFtpClient.createFtpClient(appProperties)
            ftpClient.login()

            logger.info { "Writing file part $part, path='$path'. ${links.size} Links." }
            xml.byteInputStream().use { ftpClient.storeFile(path, it) }
            ftpClient.logout()
        }
    }
}
