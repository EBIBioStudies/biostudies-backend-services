package ac.uk.ebi.biostd.submission.stats.service

import ac.uk.ebi.biostd.common.properties.PersistenceProperties
import ac.uk.ebi.biostd.persistence.common.model.CollectionStats
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ebi.ac.uk.util.collections.second
import mu.KotlinLogging
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.APPEND
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ofPattern
import kotlin.io.path.outputStream

private val logger = KotlinLogging.logger {}

class StatsReporterService(
    private val statsDataService: StatsDataService,
    private val persistenceProperties: PersistenceProperties,
) {
    suspend fun reportStats() {
        val month = OffsetDateTime.now().asPreviousMonth(1)

        logger.info { "Started calculating 'imaging' stats for month '$month'" }
        val stats = statsDataService.calculateImagingStats()
        createReport(month, IMAGING_REPORT_NAME, stats)
        logger.info { "Finished calculating 'imaging' stats for month '$month'" }

        logger.info { "Started calculating 'non-imaging' stats for month '$month'" }
        val nonImagingStats = statsDataService.calculateNonImagingStats()
        createReport(month, NON_IMAGING_REPORT_NAME, nonImagingStats)
        logger.info { "Finished calculating 'non-imaging' stats for month '$month'" }
    }

    private fun createReport(
        month: String,
        reportName: String,
        stats: CollectionStats,
    ) {
        reportTotal(month, reportName, stats.filesSize)
        reportCount(month, reportName, stats.count)
        reportDelta(month, reportName)
    }

    private fun reportTotal(
        month: String,
        reportName: String,
        value: Long,
    ) {
        logger.info { "Started generating totals report" }
        val outputPath = Paths.get(persistenceProperties.statsReportPath).resolve(TOTAL_REPORT_DIR)
        val reportPath = outputPath.resolve(reportName)
        reportPath.outputStream(APPEND).use { it.write("${month}\t$value\n".toByteArray()) }
        logger.info { "Finished generating totals report" }
    }

    private fun reportDelta(
        month: String,
        reportName: String,
    ) {
        logger.info { "Started generating delta report" }

        val totalPath = Paths.get(persistenceProperties.statsReportPath).resolve(TOTAL_REPORT_DIR)
        val deltaPath = Paths.get(persistenceProperties.statsReportPath).resolve(DELTA_REPORT_DIR)
        val reportFile = totalPath.resolve(reportName).toFile()
        val deltaFile = deltaPath.resolve(reportName)
        val lines = reportFile.readLines().takeLast(2)
        if (lines.size == 2) {
            val previousValue = getValue(lines.first())
            val currentValue = getValue(lines.second())
            val delta = currentValue - previousValue

            deltaFile.outputStream(APPEND).use { it.write("${month}\t$delta\n".toByteArray()) }
        }

        logger.info { "Finished generating delta report" }
    }

    private fun reportCount(
        month: String,
        reportName: String,
        value: Long,
    ) {
        logger.info { "Started generating count report" }
        val outputPath = Paths.get(persistenceProperties.statsReportPath).resolve(COUNT_REPORT_DIR)
        val reportPath = outputPath.resolve(reportName)
        reportPath.outputStream(APPEND).use { it.write("${month}\t$value\n".toByteArray()) }
        logger.info { "Finished generating count report" }
    }

    private fun getValue(line: String) = line.split("\t").second().toLong()

    private fun OffsetDateTime.asPreviousMonth(months: Long) = minusMonths(months).format(ofPattern(FORMAT_PATTERN))

    companion object {
        private const val FORMAT_PATTERN = "YYYYMM"
        const val DELTA_REPORT_DIR = "delta"
        const val TOTAL_REPORT_DIR = "total"
        const val COUNT_REPORT_DIR = "count"
        const val IMAGING_REPORT_NAME = "imaging.txt"
        const val NON_IMAGING_REPORT_NAME = "non_imaging.txt"
    }
}
