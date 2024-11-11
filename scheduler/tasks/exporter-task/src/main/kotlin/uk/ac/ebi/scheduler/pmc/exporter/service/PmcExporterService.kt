package uk.ac.ebi.scheduler.pmc.exporter.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.coroutines.chunked
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.scheduler.pmc.exporter.cli.BioStudiesFtpClient
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.mapping.toLink
import uk.ac.ebi.scheduler.pmc.exporter.model.Link
import uk.ac.ebi.scheduler.pmc.exporter.model.Links
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcRepository

internal const val CHUNK_SIZE = 4000

private val logger = KotlinLogging.logger {}

class PmcExporterService(
    private val pmcRepository: PmcRepository,
    private val xmlWriter: XmlMapper,
    private val ftpClient: BioStudiesFtpClient,
    private val appProperties: ApplicationProperties,
) {
    suspend fun exportPmcLinks() {
        logger.info { "Started exporting PMC links" }
        ftpClient.login()

        pmcRepository
            .findAllPmc()
            .chunked(CHUNK_SIZE)
            .collectIndexed { index, value -> writeLinks(index + 1, value) }

        ftpClient.logout()
        logger.info { "Finished exporting PMC links" }
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
        logger.info { "Writing file part $part" }

        val xml = xmlWriter.writeValueAsString(Links(links)).byteInputStream()
        val path = "${appProperties.outputPath}/${String.format(appProperties.fileName, part)}"

        ftpClient.storeFile(path, xml)
    }
}
