package uk.ac.ebi.scheduler.stats.service

import mu.KotlinLogging
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.APPEND
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.outputStream

private val logger = KotlinLogging.logger {}

class StatsReporterService(
    private val outputPath: Path,
    private val statsRepository: StatsReporterDataRepository,
) {
    fun reportStats() {
        val now = OffsetDateTime.now()
        val currentMonth = now.asPreviousMont(1)
        val previousMonth = now.asPreviousMont(2)

        reportImaging(previousMonth, currentMonth)
        reportNonImaging(previousMonth, currentMonth)
    }

    private fun reportImaging(previousMonth: String, currentMonth: String) {
        logger.info { "Started reporting imaging stats" }
        val filesSize = statsRepository.calculateImagingFilesSize()
        publishReport(previousMonth, currentMonth, filesSize, IMAGING_REPORT_NAME)
        logger.info { "Finished reporting imaging stats" }
    }

    private fun reportNonImaging(previousMonth: String, currentMonth: String) {
        logger.info { "Started reporting non-imaging stats" }
        val filesSize = statsRepository.calculateNonImagingFilesSize()
        publishReport(previousMonth, currentMonth, filesSize, NON_IMAGING_REPORT_NAME)
        logger.info { "Finished reporting non-imaging stats" }
    }

    private fun publishReport(previousMonth: String, currentMonth: String, value: Long, reportName: String) {
        val previousReportPath = outputPath.resolve("${previousMonth}_$reportName.txt")
        val currentReportPath = outputPath.resolve("${currentMonth}_$reportName.txt")

        logger.info { "Started publishing the report '$reportName' for month '$currentMonth'" }
        Files.copy(previousReportPath, currentReportPath, REPLACE_EXISTING)
        currentReportPath.outputStream(APPEND).use { it.write("${currentMonth}\t$value".toByteArray()) }
        logger.info { "Finished publishing the report '$reportName' for month '$currentMonth'" }
    }

    private fun OffsetDateTime.asPreviousMont(months: Long) =
        minusMonths(months).format(DateTimeFormatter.ofPattern(STATS_DATE_FORMAT_PATTERN))

    companion object {
        internal const val IMAGING_REPORT_NAME = "BIA"
        internal const val NON_IMAGING_REPORT_NAME = "BioStudies"
        private const val STATS_DATE_FORMAT_PATTERN = "YYYYMM"
    }
}
