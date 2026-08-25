package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.AE_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.COUNT_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.DELTA_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.IMAGING_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.NON_IMAGING_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.PUBLIC_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.TOTAL_REPORT_DIR
import ebi.ac.uk.model.constants.TEXT_PLAIN
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Paths

@RestController
@Suppress("TooManyFunctions")
@RequestMapping("/stats/report")
class StatsReportResource(
    private val appProperties: ApplicationProperties,
) {
    @GetMapping("/imaging/size/total", produces = [TEXT_PLAIN])
    suspend fun getImagingTotalSize(): String = getReport(TOTAL_REPORT_DIR, IMAGING_REPORT_NAME)

    @GetMapping("/imaging/size/delta", produces = [TEXT_PLAIN])
    suspend fun getImagingDeltaSize(): String = getReport(DELTA_REPORT_DIR, IMAGING_REPORT_NAME)

    @GetMapping("/imaging/count", produces = [TEXT_PLAIN])
    suspend fun getImagingCount(): String = getReport(COUNT_REPORT_DIR, IMAGING_REPORT_NAME)

    @GetMapping("/imaging/size/total/public", produces = [TEXT_PLAIN])
    suspend fun getPublicImagingTotalSize(): String = getPublicReport(TOTAL_REPORT_DIR, IMAGING_REPORT_NAME)

    @GetMapping("/imaging/size/delta/public", produces = [TEXT_PLAIN])
    suspend fun getPublicImagingDeltaSize(): String = getPublicReport(DELTA_REPORT_DIR, IMAGING_REPORT_NAME)

    @GetMapping("/imaging/count/public", produces = [TEXT_PLAIN])
    suspend fun getPublicImagingCount(): String = getPublicReport(COUNT_REPORT_DIR, IMAGING_REPORT_NAME)

    @GetMapping("/non-imaging/size/total", produces = [TEXT_PLAIN])
    suspend fun getNonImagingTotalSize(): String = getReport(TOTAL_REPORT_DIR, NON_IMAGING_REPORT_NAME)

    @GetMapping("/non-imaging/size/delta", produces = [TEXT_PLAIN])
    suspend fun getNonImagingDeltaSize(): String = getReport(DELTA_REPORT_DIR, NON_IMAGING_REPORT_NAME)

    @GetMapping("/non-imaging/count", produces = [TEXT_PLAIN])
    suspend fun getNonImagingCount(): String = getReport(COUNT_REPORT_DIR, NON_IMAGING_REPORT_NAME)

    @GetMapping("/non-imaging/size/total/public", produces = [TEXT_PLAIN])
    suspend fun getPublicNonImagingTotalSize(): String = getPublicReport(TOTAL_REPORT_DIR, NON_IMAGING_REPORT_NAME)

    @GetMapping("/non-imaging/size/delta/public", produces = [TEXT_PLAIN])
    suspend fun getPublicNonImagingDeltaSize(): String = getPublicReport(DELTA_REPORT_DIR, NON_IMAGING_REPORT_NAME)

    @GetMapping("/non-imaging/count/public", produces = [TEXT_PLAIN])
    suspend fun getPublicNonImagingCount(): String = getPublicReport(COUNT_REPORT_DIR, NON_IMAGING_REPORT_NAME)

    @GetMapping("/arrayexpress/size/total", produces = [TEXT_PLAIN])
    suspend fun getAETotalSize(): String = getReport(TOTAL_REPORT_DIR, AE_REPORT_NAME)

    @GetMapping("/arrayexpress/size/delta", produces = [TEXT_PLAIN])
    suspend fun getAEDeltaSize(): String = getReport(DELTA_REPORT_DIR, AE_REPORT_NAME)

    @GetMapping("/arrayexpress/count", produces = [TEXT_PLAIN])
    suspend fun getAECount(): String = getReport(COUNT_REPORT_DIR, AE_REPORT_NAME)

    @GetMapping("/arrayexpress/size/total/public", produces = [TEXT_PLAIN])
    suspend fun getPublicAETotalSize(): String = getPublicReport(TOTAL_REPORT_DIR, AE_REPORT_NAME)

    @GetMapping("/arrayexpress/size/delta/public", produces = [TEXT_PLAIN])
    suspend fun getPublicAEDeltaSize(): String = getPublicReport(DELTA_REPORT_DIR, AE_REPORT_NAME)

    @GetMapping("/arrayexpress/count/public", produces = [TEXT_PLAIN])
    suspend fun getPublicAECount(): String = getPublicReport(COUNT_REPORT_DIR, AE_REPORT_NAME)

    private fun getPublicReport(
        reportDir: String,
        reportName: String,
    ): String = loadReport("$reportDir/$PUBLIC_REPORT_DIR/$reportName")

    private fun getReport(
        reportDir: String,
        reportName: String,
    ): String = loadReport("$reportDir/$reportName")

    private fun loadReport(reportPath: String): String {
        val reportFile =
            Paths
                .get(appProperties.persistence.statsReportPath)
                .resolve(reportPath)
                .toFile()

        return if (reportFile.exists()) reportFile.readText() else ""
    }
}
