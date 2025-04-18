package uk.ac.ebi.scheduler.pmc.exporter.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.coroutines.SuspendRetryTemplate
import ebi.ac.uk.coroutines.concurrently
import ebi.ac.uk.coroutines.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.scheduler.pmc.exporter.cli.BioStudiesFtpClient
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.mapping.toLink
import uk.ac.ebi.scheduler.pmc.exporter.model.Link
import uk.ac.ebi.scheduler.pmc.exporter.model.Links
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcDataRepository
import java.util.concurrent.atomic.AtomicInteger

internal const val CHUNK_SIZE = 4000
internal const val FTP_CONCURRENCY = 10
internal const val REPORT_PROGRESS_EACH = 10_0000

private val logger = KotlinLogging.logger {}

class PmcExporterService(
    private val pmcRepository: PmcDataRepository,
    private val xmlWriter: XmlMapper,
    private val appProperties: ApplicationProperties,
    private val retryTemplate: SuspendRetryTemplate,
) {
    suspend fun updateView() {
        logger.info { "Updating pmc_export_submissions view" }
        pmcRepository.createView()
        logger.info { "Updating pmc_export_submissions finished" }
    }

    suspend fun exportPmcLinks() {
        logger.info { "Started exporting PMC links" }

        val loadedLinks = AtomicInteger(0)
        val records =
            pmcRepository
                .findAllFromView()
                .every(REPORT_PROGRESS_EACH) {
                    loadedLinks.addAndGet(REPORT_PROGRESS_EACH)
                    logger.info { "Loaded '${loadedLinks.get()}' links" }
                }.toList()

        val exportedLinks = AtomicInteger(0)
        val parts = AtomicInteger(0)
        records
            .chunked(CHUNK_SIZE)
            .asFlow()
            .concurrently(FTP_CONCURRENCY) {
                exportedLinks.addAndGet(it.size)
                writeLinks(parts.incrementAndGet(), it)
                logger.info { "Exported '${loadedLinks.get()}' links" }
            }.collect()

        logger.info { "Export Complete. Total links (exported/loaded): ${exportedLinks.get()}/${loadedLinks.get()}" }
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
