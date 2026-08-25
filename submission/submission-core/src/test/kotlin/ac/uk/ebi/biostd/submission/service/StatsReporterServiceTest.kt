package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.PersistenceProperties
import ac.uk.ebi.biostd.persistence.common.model.CollectionStats
import ac.uk.ebi.biostd.persistence.common.model.CollectionStatsReport
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.AE_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.COUNT_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.DELTA_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.IMAGING_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.NON_IMAGING_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.PUBLIC_REPORT_DIR
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

            // Verify ArrayExpress Report
            val totalAEReport = statsDir.resolve(TOTAL_REPORT_DIR).resolve(AE_REPORT_NAME)
            val expectedTotalAEReport = "202012\t$PREVIOUS_AE_TOTAL\n202101\t$AE_TOTAL"
            assertThat(totalAEReport).exists()
            assertThat(totalAEReport).hasContent(expectedTotalAEReport)

            val deltaAEReport = statsDir.resolve(DELTA_REPORT_DIR).resolve(AE_REPORT_NAME)
            val expectedDeltaAEReport = "202012\t$PREVIOUS_AE_DELTA\n202101\t$AE_DELTA"
            assertThat(deltaAEReport).exists()
            assertThat(deltaAEReport).hasContent(expectedDeltaAEReport)

            val countAEReport = statsDir.resolve(COUNT_REPORT_DIR).resolve(AE_REPORT_NAME)
            val expectedCountAEReport = "202012\t$PREVIOUS_AE_COUNT\n202101\t$AE_COUNT"
            assertThat(countAEReport).exists()
            assertThat(countAEReport).hasContent(expectedCountAEReport)

            // Verify Imaging Report
            val totalImagingReport = statsDir.resolve(TOTAL_REPORT_DIR).resolve(IMAGING_REPORT_NAME)
            val expectedTotalImagingReport = "202012\t$PREVIOUS_IMAGING_TOTAL\n202101\t$IMAGING_TOTAL"
            assertThat(totalImagingReport).exists()
            assertThat(totalImagingReport).hasContent(expectedTotalImagingReport)

            val deltaImagingReport = statsDir.resolve(DELTA_REPORT_DIR).resolve(IMAGING_REPORT_NAME)
            val expectedDeltaImagingReport = "202012\t$PREVIOUS_IMAGING_DELTA\n202101\t$IMAGING_DELTA"
            assertThat(deltaImagingReport).exists()
            assertThat(deltaImagingReport).hasContent(expectedDeltaImagingReport)

            val countImagingReport = statsDir.resolve(COUNT_REPORT_DIR).resolve(IMAGING_REPORT_NAME)
            val expectedCountImagingReport = "202012\t$PREVIOUS_IMAGING_COUNT\n202101\t$IMAGING_COUNT"
            assertThat(countImagingReport).exists()
            assertThat(countImagingReport).hasContent(expectedCountImagingReport)

            // Verify Imaging Report
            val totalNonImagingReport = statsDir.resolve(TOTAL_REPORT_DIR).resolve(NON_IMAGING_REPORT_NAME)
            val expectedTotalNonImagingReport = "202012\t$PREVIOUS_NON_IMAGING_TOTAL\n202101\t$NON_IMAGING_TOTAL"
            assertThat(totalNonImagingReport).exists()
            assertThat(totalNonImagingReport).hasContent(expectedTotalNonImagingReport)

            val deltaNonImagingReport = statsDir.resolve(DELTA_REPORT_DIR).resolve(NON_IMAGING_REPORT_NAME)
            val expectedDeltaNonImagingReport = "202012\t$PREVIOUS_NON_IMAGING_DELTA\n202101\t$NON_IMAGING_DELTA"
            assertThat(deltaNonImagingReport).exists()
            assertThat(deltaNonImagingReport).hasContent(expectedDeltaNonImagingReport)

            val countNonImagingReport = statsDir.resolve(COUNT_REPORT_DIR).resolve(NON_IMAGING_REPORT_NAME)
            val expectedCountNonImagingReport = "202012\t$PREVIOUS_NON_IMAGING_COUNT\n202101\t$NON_IMAGING_COUNT"
            assertThat(countNonImagingReport).exists()
            assertThat(countNonImagingReport).hasContent(expectedCountNonImagingReport)

            // Verify public ArrayExpress report
            assertPublicReports(AE_REPORT_NAME, PREVIOUS_PUBLIC_AE_COUNT, PUBLIC_AE_COUNT, PREVIOUS_PUBLIC_AE_TOTAL, PUBLIC_AE_TOTAL)

            // Verify public imaging report
            assertPublicReports(
                IMAGING_REPORT_NAME,
                PREVIOUS_PUBLIC_IMAGING_COUNT,
                PUBLIC_IMAGING_COUNT,
                PREVIOUS_PUBLIC_IMAGING_TOTAL,
                PUBLIC_IMAGING_TOTAL,
            )

            // Verify public non-imaging report
            assertPublicReports(
                NON_IMAGING_REPORT_NAME,
                PREVIOUS_PUBLIC_NON_IMAGING_COUNT,
                PUBLIC_NON_IMAGING_COUNT,
                PREVIOUS_PUBLIC_NON_IMAGING_TOTAL,
                PUBLIC_NON_IMAGING_TOTAL,
            )
        }

    private fun assertPublicReports(
        reportName: String,
        previousCount: Long,
        count: Long,
        previousTotal: Long,
        total: Long,
    ) {
        assertThat(statsDir.resolve(TOTAL_REPORT_DIR).resolve(PUBLIC_REPORT_DIR).resolve(reportName))
            .hasContent("202012\t$previousTotal\n202101\t$total")
        assertThat(statsDir.resolve(DELTA_REPORT_DIR).resolve(PUBLIC_REPORT_DIR).resolve(reportName))
            .hasContent("202012\t$PREVIOUS_PUBLIC_DELTA\n202101\t${total - previousTotal}")
        assertThat(statsDir.resolve(COUNT_REPORT_DIR).resolve(PUBLIC_REPORT_DIR).resolve(reportName))
            .hasContent("202012\t$previousCount\n202101\t$count")
    }

    private fun setUpDate() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns OffsetDateTime.of(2021, 2, 1, 0, 0, 0, 0, UTC)
    }

    private fun setUpStats() {
        coEvery { statsDataService.calculateAEStats() } returns
            CollectionStatsReport(
                all = CollectionStats(AE_COUNT, AE_TOTAL),
                public = CollectionStats(PUBLIC_AE_COUNT, PUBLIC_AE_TOTAL),
            )
        coEvery { statsDataService.calculateImagingStats() } returns
            CollectionStatsReport(
                all = CollectionStats(IMAGING_COUNT, IMAGING_TOTAL),
                public = CollectionStats(PUBLIC_IMAGING_COUNT, PUBLIC_IMAGING_TOTAL),
            )
        coEvery { statsDataService.calculateNonImagingStats() } returns
            CollectionStatsReport(
                all = CollectionStats(NON_IMAGING_COUNT, NON_IMAGING_TOTAL),
                public = CollectionStats(PUBLIC_NON_IMAGING_COUNT, PUBLIC_NON_IMAGING_TOTAL),
            )

        val deltaReport = statsDir.createDirectory(DELTA_REPORT_DIR)
        deltaReport.createFile(AE_REPORT_NAME, "202012\t$PREVIOUS_AE_DELTA\n")
        deltaReport.createFile(IMAGING_REPORT_NAME, "202012\t$PREVIOUS_IMAGING_DELTA\n")
        deltaReport.createFile(NON_IMAGING_REPORT_NAME, "202012\t$PREVIOUS_NON_IMAGING_DELTA\n")

        val countReport = statsDir.createDirectory(COUNT_REPORT_DIR)
        countReport.createFile(AE_REPORT_NAME, "202012\t$PREVIOUS_AE_COUNT\n")
        countReport.createFile(IMAGING_REPORT_NAME, "202012\t$PREVIOUS_IMAGING_COUNT\n")
        countReport.createFile(NON_IMAGING_REPORT_NAME, "202012\t$PREVIOUS_NON_IMAGING_COUNT\n")

        val totalReport = statsDir.createDirectory(TOTAL_REPORT_DIR)
        totalReport.createFile(AE_REPORT_NAME, "202012\t$PREVIOUS_AE_TOTAL\n")
        totalReport.createFile(IMAGING_REPORT_NAME, "202012\t$PREVIOUS_IMAGING_TOTAL\n")
        totalReport.createFile(NON_IMAGING_REPORT_NAME, "202012\t$PREVIOUS_NON_IMAGING_TOTAL\n")

        val publicDeltaReport = deltaReport.createDirectory(PUBLIC_REPORT_DIR)
        publicDeltaReport.createFile(AE_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_DELTA\n")
        publicDeltaReport.createFile(IMAGING_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_DELTA\n")
        publicDeltaReport.createFile(NON_IMAGING_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_DELTA\n")

        val publicCountReport = countReport.createDirectory(PUBLIC_REPORT_DIR)
        publicCountReport.createFile(AE_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_AE_COUNT\n")
        publicCountReport.createFile(IMAGING_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_IMAGING_COUNT\n")
        publicCountReport.createFile(NON_IMAGING_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_NON_IMAGING_COUNT\n")

        val publicTotalReport = totalReport.createDirectory(PUBLIC_REPORT_DIR)
        publicTotalReport.createFile(AE_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_AE_TOTAL\n")
        publicTotalReport.createFile(IMAGING_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_IMAGING_TOTAL\n")
        publicTotalReport.createFile(NON_IMAGING_REPORT_NAME, "202012\t$PREVIOUS_PUBLIC_NON_IMAGING_TOTAL\n")
    }

    private fun setUpPaths() {
        every { persistenceProperties.statsReportPath } returns statsDir.absolutePath
    }

    companion object {
        private const val AE_DELTA = 101L
        private const val AE_COUNT = 547L
        private const val AE_TOTAL = 87719120L
        private const val PREVIOUS_AE_COUNT = 352L
        private const val PREVIOUS_AE_DELTA = 12L
        private const val PREVIOUS_AE_TOTAL = 87719019L

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

        private const val PREVIOUS_PUBLIC_DELTA = 17L
        private const val PUBLIC_AE_COUNT = 500L
        private const val PUBLIC_AE_TOTAL = 700L
        private const val PREVIOUS_PUBLIC_AE_COUNT = 400L
        private const val PREVIOUS_PUBLIC_AE_TOTAL = 600L

        private const val PUBLIC_IMAGING_COUNT = 150L
        private const val PUBLIC_IMAGING_TOTAL = 800L
        private const val PREVIOUS_PUBLIC_IMAGING_COUNT = 100L
        private const val PREVIOUS_PUBLIC_IMAGING_TOTAL = 900L

        private const val PUBLIC_NON_IMAGING_COUNT = 50L
        private const val PUBLIC_NON_IMAGING_TOTAL = 300L
        private const val PREVIOUS_PUBLIC_NON_IMAGING_COUNT = 40L
        private const val PREVIOUS_PUBLIC_NON_IMAGING_TOTAL = 200L
    }
}
