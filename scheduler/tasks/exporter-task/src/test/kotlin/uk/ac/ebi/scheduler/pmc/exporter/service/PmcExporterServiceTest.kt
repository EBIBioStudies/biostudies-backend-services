package uk.ac.ebi.scheduler.pmc.exporter.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.apache.commons.net.ftp.FTPClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.config.Ftp
import uk.ac.ebi.scheduler.pmc.exporter.model.Links
import uk.ac.ebi.scheduler.pmc.exporter.model.PMC_SOURCE
import uk.ac.ebi.scheduler.pmc.exporter.model.PROVIDER_ID
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcRepository
import java.io.ByteArrayInputStream

@ExtendWith(MockKExtension::class)
class PmcExporterServiceTest(
    @MockK private val ftpClient: FTPClient,
    @MockK private val xmlWriter: XmlMapper,
    @MockK private val pmcRepository: PmcRepository,
    @MockK private val appProperties: ApplicationProperties,
) {
    private val testInstance = PmcExporterService(pmcRepository, xmlWriter, ftpClient, appProperties)

    @BeforeEach
    fun beforeEach() {
        every { appProperties.fileName } returns "TestLinks.part%03d.xml"
        every { appProperties.outputPath } returns "test/links"
    }

    @Test
    fun `export pmc links`() {
        runBlocking {
            val linksSlot = slot<Links>()
            val pageableSlot = slot<Pageable>()
            val xmlSlot = slot<ByteArrayInputStream>()
            val pmcData = PmcData("S-EPMC123", "Test PMC")

            setUpFtpClient()
            every { xmlWriter.writeValueAsString(capture(linksSlot)) } returns "serialized"
            every { pmcRepository.findAllPmc(capture(pageableSlot)) } returns PageImpl(listOf(pmcData))
            every { ftpClient.storeFile("test/links/TestLinks.part001.xml", capture(xmlSlot)) } returns true

            testInstance.exportPmcLinks()

            val xml = xmlSlot.captured
            val links = linksSlot.captured
            val pageable = pageableSlot.captured

            assertThat(links.link).hasSize(1)

            val link = links.link.first()
            assertThat(link.record.id).isEqualTo("PMC123")
            assertThat(link.record.source).isEqualTo(PMC_SOURCE)
            assertThat(link.providerId).isEqualTo(PROVIDER_ID)
            assertThat(link.resource.title).isEqualTo("Test PMC")
            assertThat(link.resource.url).isEqualTo("http://www.ebi.ac.uk/biostudies/studies/S-EPMC123?xr=true")

            verify(exactly = 1) {
                pmcRepository.findAllPmc(pageable)
                ftpClient.storeFile("test/links/TestLinks.part001.xml", xml)
            }
        }
    }

    private fun setUpFtpClient() {
        val ftpProperties = Ftp().apply {
            host = "localhost"
            user = "test"
            password = "admin"
            port = 21
        }

        every { ftpClient.logout() } returns true
        every { appProperties.ftp } returns ftpProperties
        every { ftpClient.disconnect() } answers { nothing }
        every { ftpClient.login("test", "admin") } returns true
        every { ftpClient.connect("localhost", 21) } answers { nothing }
    }
}
