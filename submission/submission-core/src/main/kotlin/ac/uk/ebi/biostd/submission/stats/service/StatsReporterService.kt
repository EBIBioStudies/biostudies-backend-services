package ac.uk.ebi.biostd.submission.stats.service

import ac.uk.ebi.biostd.common.properties.PersistenceProperties
import ac.uk.ebi.biostd.persistence.common.model.CollectionStatsReport
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ebi.ac.uk.util.collections.second
import mu.KotlinLogging
import java.io.File
import java.nio.file.Path
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

        logger.info { "Started calculating 'Array Express' stats for month '$month'" }
        val aeStats = statsDataService.calculateAEStats()
        createReport(month, AE_REPORT_NAME, aeStats)
        logger.info { "Finished calculating 'Array Express' stats for month '$month'" }

        logger.info { "Started calculating 'Imaging' stats for month '$month'" }
        val imagingStats = statsDataService.calculateImagingStats()
        createReport(month, IMAGING_REPORT_NAME, imagingStats)
        logger.info { "Finished calculating 'Imaging' stats for month '$month'" }

        logger.info { "Started calculating 'Non Imaging' stats for month '$month'" }
        val nonImagingStats = statsDataService.calculateNonImagingStats()
        createReport(month, NON_IMAGING_REPORT_NAME, nonImagingStats)
        logger.info { "Finished calculating 'Non Imaging' stats for month '$month'" }
    }

    private fun createReport(
        month: String,
        reportName: String,
        stats: CollectionStatsReport,
    ) {
        reportTotal(month, reportName, stats)
        reportCount(month, reportName, stats)
        reportDelta(month, reportName)
    }

    private fun reportTotal(
        month: String,
        reportName: String,
        stats: CollectionStatsReport,
    ) {
        logger.info { "Started generating totals report" }
        val outputPath = Paths.get(persistenceProperties.statsReportPath).resolve(TOTAL_REPORT_DIR)
        val publicPath = outputPath.resolve(PUBLIC_REPORT_DIR)
        val reportPath = outputPath.resolve(reportName)
        val publicReportPath = publicPath.resolve(reportName)
        reportPath.outputStream(APPEND).use { it.write("${month}\t${stats.all.filesSize}\n".toByteArray()) }
        publicReportPath.outputStream(APPEND).use { it.write("${month}\t${stats.public.filesSize}\n".toByteArray()) }
        logger.info { "Finished generating totals report" }
    }

    private fun reportDelta(
        month: String,
        reportName: String,
    ) {
        logger.info { "Started generating delta report" }

        val totalPath = Paths.get(persistenceProperties.statsReportPath).resolve(TOTAL_REPORT_DIR)
        val deltaPath = Paths.get(persistenceProperties.statsReportPath).resolve(DELTA_REPORT_DIR)
        val totalPathPublic = totalPath.resolve(PUBLIC_REPORT_DIR)
        val deltaPathPublic = deltaPath.resolve(PUBLIC_REPORT_DIR)
        val reportFile = totalPath.resolve(reportName).toFile()
        val deltaFile = deltaPath.resolve(reportName)
        val reportFilePublic = totalPathPublic.resolve(reportName).toFile()
        val deltaFilePublic = deltaPathPublic.resolve(reportName)

        generateDelta(month, reportFile, deltaFile)
        generateDelta(month, reportFilePublic, deltaFilePublic)

        logger.info { "Finished generating delta report" }
    }

    private fun generateDelta(
        month: String,
        reportFile: File,
        deltaFile: Path,
    ) {
        val lines = reportFile.readLines().takeLast(2)
        if (lines.size == 2) {
            val previousValue = getValue(lines.first())
            val currentValue = getValue(lines.second())
            val delta = currentValue - previousValue

            deltaFile.outputStream(APPEND).use { it.write("${month}\t$delta\n".toByteArray()) }
        }
    }

    private fun reportCount(
        month: String,
        reportName: String,
        stats: CollectionStatsReport,
    ) {
        logger.info { "Started generating count report" }
        val outputPath = Paths.get(persistenceProperties.statsReportPath).resolve(COUNT_REPORT_DIR)
        val publicPath = outputPath.resolve(PUBLIC_REPORT_DIR)
        val reportPath = outputPath.resolve(reportName)
        val publicReportPath = publicPath.resolve(reportName)
        reportPath.outputStream(APPEND).use { it.write("${month}\t${stats.all.count}\n".toByteArray()) }
        publicReportPath.outputStream(APPEND).use { it.write("${month}\t${stats.public.count}\n".toByteArray()) }
        logger.info { "Finished generating count report" }
    }

    private fun getValue(line: String) = line.split("\t").second().toLong()

    private fun OffsetDateTime.asPreviousMonth(months: Long) = minusMonths(months).format(ofPattern(FORMAT_PATTERN))

    companion object {
        private const val FORMAT_PATTERN = "yyyyMM"
        const val DELTA_REPORT_DIR = "delta"
        const val TOTAL_REPORT_DIR = "total"
        const val COUNT_REPORT_DIR = "count"
        const val PUBLIC_REPORT_DIR = "public"
        const val AE_REPORT_NAME = "arrayexpress.txt"
        const val IMAGING_REPORT_NAME = "imaging.txt"
        const val NON_IMAGING_REPORT_NAME = "non_imaging.txt"
    }
}
