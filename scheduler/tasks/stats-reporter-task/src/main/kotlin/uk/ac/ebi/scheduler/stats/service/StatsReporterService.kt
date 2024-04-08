package uk.ac.ebi.scheduler.stats.service

import mu.KotlinLogging
import uk.ac.ebi.scheduler.stats.config.ApplicationProperties
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.APPEND
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ofPattern
import kotlin.io.path.outputStream

private val logger = KotlinLogging.logger {}

class StatsReporterService(
    private val appProperties: ApplicationProperties,
    private val statsRepository: StatsReporterDataRepository,
) {
    suspend fun reportStats() {
        val now = OffsetDateTime.now()
        val currentMonth = now.asPreviousMonth(1)
        val previousMonth = now.asPreviousMonth(2)

        createImagingReport(previousMonth, currentMonth)
        createNonImagingReport(previousMonth, currentMonth)
    }

    private suspend fun createImagingReport(
        previousMonth: String,
        currentMonth: String,
    ): Path {
        logger.info { "Started calculating imaging stats" }
        val filesSize = statsRepository.calculateImagingFilesSize()
        val report = createReportFile(previousMonth, currentMonth, filesSize, IMAGING_REPORT_NAME)
        logger.info { "Finished calculating imaging stats" }

        return report
    }

    private suspend fun createNonImagingReport(
        previousMonth: String,
        currentMonth: String,
    ): Path {
        logger.info { "Started calculating non-imaging stats" }
        val filesSize = statsRepository.calculateNonImagingFilesSize()
        val report = createReportFile(previousMonth, currentMonth, filesSize, NON_IMAGING_REPORT_NAME)
        logger.info { "Finished calculating non-imaging stats" }

        return report
    }

    private fun createReportFile(
        previousMonth: String,
        currentMonth: String,
        value: Long,
        reportName: String,
    ): Path {
        val outputPath = Paths.get(appProperties.publishPath)
        val previousReportPath = outputPath.resolve("${previousMonth}_$reportName.txt")
        val currentReportPath = outputPath.resolve("${currentMonth}_$reportName.txt")

        logger.info { "Started publishing report file '$reportName' for month '$currentMonth'" }
        Files.copy(previousReportPath, currentReportPath, REPLACE_EXISTING)
        currentReportPath.outputStream(APPEND).use { it.write("\n${currentMonth}\t$value".toByteArray()) }
        logger.info { "Finished publishing report file '$reportName' for month '$currentMonth'" }

        return currentReportPath
    }

    private fun OffsetDateTime.asPreviousMonth(months: Long) = minusMonths(months).format(ofPattern(FORMAT_PATTERN))

    companion object {
        private const val FORMAT_PATTERN = "YYYYMM"
        internal const val IMAGING_REPORT_NAME = "BIA"
        internal const val NON_IMAGING_REPORT_NAME = "BioStudies"
    }
}
