package uk.ac.ebi.scheduler.pmc.exporter.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import mu.KotlinLogging
import uk.ac.ebi.scheduler.pmc.exporter.cli.BioStudiesFtpClient
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.mapping.toLink
import uk.ac.ebi.scheduler.pmc.exporter.model.Link
import uk.ac.ebi.scheduler.pmc.exporter.model.Links
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcRepository
import uk.ac.ebi.scheduler.pmc.exporter.persistence.pageIterator

internal const val CHUNK_SIZE = 4000

private val logger = KotlinLogging.logger {}

class PmcExporterService(
    pmcRepository: PmcRepository,
    private val xmlWriter: XmlMapper,
    private val ftpClient: BioStudiesFtpClient,
    private val appProperties: ApplicationProperties,
) {
    private val pmcDataIterator = pageIterator(pmcRepository)

    fun exportPmcLinks() {
        logger.info { "Started exporting PMC links" }

        ftpClient.login()

        pmcDataIterator
            .asSequence()
            .mapIndexed { part, page -> writeLinks(part + 1, page.content) }
            .toList()

        ftpClient.logout()

        logger.info { "Finished exporting PMC links" }
    }

    private fun writeLinks(part: Int, submissions: List<PmcData>) {
        logger.info { "Exporting PMC links part $part" }
        val links = submissions.map { it.toLink() }

        write(part, links)
    }

    private fun write(part: Int, links: List<Link>) {
        logger.info { "Writing file part $part" }

        val xml = xmlWriter.writeValueAsString(Links(links)).byteInputStream()
        val path = "${appProperties.outputPath}/${String.format(appProperties.fileName, part)}"

        ftpClient.storeFile(path, xml)
    }
}
