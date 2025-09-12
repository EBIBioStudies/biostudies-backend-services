package uk.ac.ebi.scheduler.stats.service

import ebi.ac.uk.io.ext.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.scheduler.stats.config.ApplicationProperties
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository
import uk.ac.ebi.scheduler.stats.service.StatsReporterService.Companion.IMAGING_REPORT_NAME
import uk.ac.ebi.scheduler.stats.service.StatsReporterService.Companion.NON_IMAGING_REPORT_NAME
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class StatsReporterServiceTest(
    tempFolder: TemporaryFolder,
    @param:MockK private val appProperties: ApplicationProperties,
    @param:MockK private val statsRepository: StatsReporterDataRepository,
) {
    private val publishFolder = tempFolder.createDirectory("publish")
    private val testInstance = StatsReporterService(appProperties, statsRepository)

    @BeforeEach
    fun beforeEach() {
        setUpDate()
        setUpStats()
        setUpPaths()
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun reportStats() =
        runTest {
            testInstance.reportStats()

            val imagingReport = publishFolder.resolve("202307_$IMAGING_REPORT_NAME.txt")
            val expectedImagingReport = "202306\t$PREVIOUS_IMAGING_FILES_SIZE\n202307\t$IMAGING_FILES_SIZE"
            assertThat(imagingReport.exists()).isTrue()
            assertThat(imagingReport).hasContent(expectedImagingReport)

            val nonImagingReport = publishFolder.resolve("202307_$NON_IMAGING_REPORT_NAME.txt")
            val expectedNonImagingReport = "202306\t$PREVIOUS_NON_IMAGING_FILES_SIZE\n202307\t$NON_IMAGING_FILES_SIZE"
            assertThat(nonImagingReport.exists()).isTrue()
            assertThat(nonImagingReport).hasContent(expectedNonImagingReport)
        }

    private fun setUpDate() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns OffsetDateTime.of(2023, 8, 24, 0, 0, 0, 0, UTC)
    }

    private fun setUpStats() {
        coEvery { statsRepository.calculateImagingFilesSize() } returns IMAGING_FILES_SIZE
        coEvery { statsRepository.calculateNonImagingFilesSize() } returns NON_IMAGING_FILES_SIZE

        publishFolder.createFile("202306_$IMAGING_REPORT_NAME.txt", "202306\t$PREVIOUS_IMAGING_FILES_SIZE")
        publishFolder.createFile("202306_$NON_IMAGING_REPORT_NAME.txt", "202306\t$PREVIOUS_NON_IMAGING_FILES_SIZE")
    }

    private fun setUpPaths() {
        every { appProperties.publishPath } returns publishFolder.absolutePath
    }

    companion object {
        private const val IMAGING_FILES_SIZE = 68271812L
        private const val PREVIOUS_IMAGING_FILES_SIZE = 68271714L

        private const val NON_IMAGING_FILES_SIZE = 9181927472L
        private const val PREVIOUS_NON_IMAGING_FILES_SIZE = 9181921234L
    }
}
