package uk.ac.ebi.scheduler.exporter.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ExporterServiceTest(
    @MockK private val pmcExporterService: PmcExporterService,
    @MockK private val publicOnlyExporterService: PublicOnlyExporterService
) {
    private val testInstance = ExporterService(pmcExporterService, publicOnlyExporterService)

    @BeforeEach
    fun beforeEach() {
        every { publicOnlyExporterService.exportPublicSubmissions() } answers { nothing }
        every { runBlocking { pmcExporterService.exportPmcLinks() } } answers { nothing }
    }

    @Test
    fun `export pmc`() {
        runBlocking {
            testInstance.exportPmc()
        }
        verify(exactly = 1) { runBlocking { pmcExporterService.exportPmcLinks() } }
    }

    @Test
    fun `export public only`() {
        testInstance.exportPublicOnly()
        verify(exactly = 1) { publicOnlyExporterService.exportPublicSubmissions() }
    }
}
