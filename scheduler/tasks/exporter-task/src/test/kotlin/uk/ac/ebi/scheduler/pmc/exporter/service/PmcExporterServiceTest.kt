package uk.ac.ebi.scheduler.pmc.exporter.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.scheduler.pmc.exporter.cli.BioStudiesFtpClient
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.model.Links
import uk.ac.ebi.scheduler.pmc.exporter.model.PMC_SOURCE
import uk.ac.ebi.scheduler.pmc.exporter.model.PROVIDER_ID
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData
import uk.ac.ebi.scheduler.pmc.exporter.persistence.PmcRepository
import java.io.ByteArrayInputStream

@ExtendWith(MockKExtension::class)
class PmcExporterServiceTest(
    @MockK private val xmlWriter: XmlMapper,
    @MockK private val pmcRepository: PmcRepository,
    @MockK private val ftpClient: BioStudiesFtpClient,
    @MockK private val appProperties: ApplicationProperties,
) {
    private val testInstance = PmcExporterService(pmcRepository, xmlWriter, ftpClient, appProperties)

    @BeforeEach
    fun beforeEach() {
        every { appProperties.fileName } returns "TestLinks.part%03d.xml"
        every { appProperties.outputPath } returns "test/links"
    }

    @Test
    fun `export pmc links`() =
        runTest {
            val linksSlot = slot<Links>()
            val xmlSlot = slot<ByteArrayInputStream>()
            val pmcData = PmcData("S-EPMC123", "Test PMC")

            every { ftpClient.login() } answers { nothing }
            every { ftpClient.logout() } answers { nothing }
            every { xmlWriter.writeValueAsString(capture(linksSlot)) } returns "serialized"
            every { pmcRepository.findAllPmc() } returns flowOf(pmcData)
            every { ftpClient.storeFile("test/links/TestLinks.part001.xml", capture(xmlSlot)) } answers { nothing }

            testInstance.exportPmcLinks()

            val xml = xmlSlot.captured
            val links = linksSlot.captured

            assertThat(links.link).hasSize(1)

            val link = links.link.first()
            assertThat(link.record.id).isEqualTo("PMC123")
            assertThat(link.record.source).isEqualTo(PMC_SOURCE)
            assertThat(link.providerId).isEqualTo(PROVIDER_ID)
            assertThat(link.resource.title).isEqualTo("Test PMC")
            assertThat(link.resource.url).isEqualTo("http://www.ebi.ac.uk/biostudies/studies/S-EPMC123?xr=true")

            verify(exactly = 1) {
                ftpClient.login()
                ftpClient.logout()
                pmcRepository.findAllPmc()
                ftpClient.storeFile("test/links/TestLinks.part001.xml", xml)
            }
        }
}
