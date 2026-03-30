package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.PersistenceProperties
import ac.uk.ebi.biostd.persistence.common.model.CollectionStats
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.COUNT_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.DELTA_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.IMAGING_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.NON_IMAGING_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.TOTAL_REPORT_DIR
import ebi.ac.uk.io.ext.createDirectory
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
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class StatsReporterServiceTest(
    tempFolder: TemporaryFolder,
    @param:MockK private val statsDataService: StatsDataService,
    @param:MockK private val persistenceProperties: PersistenceProperties,
) {
    private val statsDir = tempFolder.createDirectory("stats")
    private val testInstance = StatsReporterService(statsDataService, persistenceProperties)

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

            val totalImagingReport = statsDir.resolve(TOTAL_REPORT_DIR).resolve(IMAGING_REPORT_NAME)
            val expectedTotalImagingReport = "202306\t$PREVIOUS_IMAGING_TOTAL\n202307\t$IMAGING_TOTAL"
            assertThat(totalImagingReport).exists()
            assertThat(totalImagingReport).hasContent(expectedTotalImagingReport)

            val totalNonImagingReport = statsDir.resolve(TOTAL_REPORT_DIR).resolve(NON_IMAGING_REPORT_NAME)
            val expectedTotalNonImagingReport = "202306\t$PREVIOUS_NON_IMAGING_TOTAL\n202307\t$NON_IMAGING_TOTAL"
            assertThat(totalNonImagingReport).exists()
            assertThat(totalNonImagingReport).hasContent(expectedTotalNonImagingReport)

            val deltaImagingReport = statsDir.resolve(DELTA_REPORT_DIR).resolve(IMAGING_REPORT_NAME)
            val expectedDeltaImagingReport = "202306\t$PREVIOUS_IMAGING_DELTA\n202307\t$IMAGING_DELTA"
            assertThat(deltaImagingReport).exists()
            assertThat(deltaImagingReport).hasContent(expectedDeltaImagingReport)

            val deltaNonImagingReport = statsDir.resolve(DELTA_REPORT_DIR).resolve(NON_IMAGING_REPORT_NAME)
            val expectedDeltaNonImagingReport = "202306\t$PREVIOUS_NON_IMAGING_DELTA\n202307\t$NON_IMAGING_DELTA"
            assertThat(deltaNonImagingReport).exists()
            assertThat(deltaNonImagingReport).hasContent(expectedDeltaNonImagingReport)

            val countImagingReport = statsDir.resolve(COUNT_REPORT_DIR).resolve(IMAGING_REPORT_NAME)
            val expectedCountImagingReport = "202306\t$PREVIOUS_IMAGING_COUNT\n202307\t$IMAGING_COUNT"
            assertThat(countImagingReport).exists()
            assertThat(countImagingReport).hasContent(expectedCountImagingReport)

            val countNonImagingReport = statsDir.resolve(COUNT_REPORT_DIR).resolve(NON_IMAGING_REPORT_NAME)
            val expectedCountNonImagingReport = "202306\t$PREVIOUS_NON_IMAGING_COUNT\n202307\t$NON_IMAGING_COUNT"
            assertThat(countNonImagingReport).exists()
            assertThat(countNonImagingReport).hasContent(expectedCountNonImagingReport)
        }

    private fun setUpDate() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns OffsetDateTime.of(2023, 8, 24, 0, 0, 0, 0, UTC)
    }

    private fun setUpStats() {
        coEvery { statsDataService.calculateImagingStats() } returns CollectionStats(IMAGING_COUNT, IMAGING_TOTAL)
        coEvery {
            statsDataService.calculateNonImagingStats()
        } returns CollectionStats(NON_IMAGING_COUNT, NON_IMAGING_TOTAL)

        val deltaReport = statsDir.createDirectory(DELTA_REPORT_DIR)
        deltaReport.createFile(IMAGING_REPORT_NAME, "202306\t$PREVIOUS_IMAGING_DELTA\n")
        deltaReport.createFile(NON_IMAGING_REPORT_NAME, "202306\t$PREVIOUS_NON_IMAGING_DELTA\n")

        val countReport = statsDir.createDirectory(COUNT_REPORT_DIR)
        countReport.createFile(IMAGING_REPORT_NAME, "202306\t$PREVIOUS_IMAGING_COUNT\n")
        countReport.createFile(NON_IMAGING_REPORT_NAME, "202306\t$PREVIOUS_NON_IMAGING_COUNT\n")

        val totalReport = statsDir.createDirectory(TOTAL_REPORT_DIR)
        totalReport.createFile(IMAGING_REPORT_NAME, "202306\t$PREVIOUS_IMAGING_TOTAL\n")
        totalReport.createFile(NON_IMAGING_REPORT_NAME, "202306\t$PREVIOUS_NON_IMAGING_TOTAL\n")
    }

    private fun setUpPaths() {
        every { persistenceProperties.statsReportPath } returns statsDir.absolutePath
    }

    companion object {
        private const val IMAGING_DELTA = 98L
        private const val IMAGING_COUNT = 156L
        private const val IMAGING_TOTAL = 68271812L
        private const val PREVIOUS_IMAGING_COUNT = 102L
        private const val PREVIOUS_IMAGING_DELTA = 1234L
        private const val PREVIOUS_IMAGING_TOTAL = 68271714L

        private const val NON_IMAGING_COUNT = 98L
        private const val NON_IMAGING_DELTA = 6238L
        private const val NON_IMAGING_TOTAL = 9181927472L
        private const val PREVIOUS_NON_IMAGING_COUNT = 76L
        private const val PREVIOUS_NON_IMAGING_DELTA = 123456L
        private const val PREVIOUS_NON_IMAGING_TOTAL = 9181921234L
    }
}
