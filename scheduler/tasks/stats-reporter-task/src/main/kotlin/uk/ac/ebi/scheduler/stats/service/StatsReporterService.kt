package uk.ac.ebi.scheduler.stats.service

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.DataMoverQueue
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.TWO_GB
import mu.KotlinLogging
import uk.ac.ebi.scheduler.stats.config.ApplicationProperties
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardOpenOption.APPEND
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.outputStream

private val logger = KotlinLogging.logger {}

class StatsReporterService(
    private val appProperties: ApplicationProperties,
    private val clusterOperations: ClusterOperations,
    private val statsRepository: StatsReporterDataRepository,
) {
    fun reportStats() {
        val now = OffsetDateTime.now()
        val currentMonth = now.asPreviousMont(1)
        val previousMonth = now.asPreviousMont(2)

        val imagingReport = createImagingReport(previousMonth, currentMonth)
        val nonImagingReport = createNonImagingReport(previousMonth, currentMonth)
        publishReports(imagingReport, nonImagingReport)
    }

    private fun createImagingReport(previousMonth: String, currentMonth: String): Path {
        logger.info { "Started reporting imaging stats" }
        val filesSize = statsRepository.calculateImagingFilesSize()
        val report = createReportFile(previousMonth, currentMonth, filesSize, IMAGING_REPORT_NAME)
        logger.info { "Finished reporting imaging stats" }

        return report
    }

    private fun createNonImagingReport(previousMonth: String, currentMonth: String): Path {
        logger.info { "Started reporting non-imaging stats" }
        val filesSize = statsRepository.calculateNonImagingFilesSize()
        val report = createReportFile(previousMonth, currentMonth, filesSize, NON_IMAGING_REPORT_NAME)
        logger.info { "Finished reporting non-imaging stats" }

        return report
    }

    private fun createReportFile(previousMonth: String, currentMonth: String, value: Long, reportName: String): Path {
        val outputPath = Paths.get(appProperties.outputPath)
        val previousReportPath = outputPath.resolve("${previousMonth}_$reportName.txt")
        val currentReportPath = outputPath.resolve("${currentMonth}_$reportName.txt")

        logger.info { "Started publishing the report '$reportName' for month '$currentMonth'" }
        Files.copy(previousReportPath, currentReportPath, REPLACE_EXISTING)
        currentReportPath.outputStream(APPEND).use { it.write("${currentMonth}\t$value".toByteArray()) }
        logger.info { "Finished publishing the report '$reportName' for month '$currentMonth'" }

        return currentReportPath
    }

    private fun publishReports(imagingReport: Path, nonImagingReport: Path): Job {
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                ram = TWO_GB,
                queue = DataMoverQueue,
                command = "scp $imagingReport $nonImagingReport ${appProperties.publishPath}"
            )
        )

        return jobTry.fold({ throw it }, { it.apply { logger.info { "Submitted job $it" } } })
    }

    private fun OffsetDateTime.asPreviousMont(months: Long) =
        minusMonths(months).format(DateTimeFormatter.ofPattern(STATS_DATE_FORMAT_PATTERN))

    companion object {
        internal const val IMAGING_REPORT_NAME = "BIA"
        internal const val NON_IMAGING_REPORT_NAME = "BioStudies"
        private const val STATS_DATE_FORMAT_PATTERN = "YYYYMM"
    }
}
