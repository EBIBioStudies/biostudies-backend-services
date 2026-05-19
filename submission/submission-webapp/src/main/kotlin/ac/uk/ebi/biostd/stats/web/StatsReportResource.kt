package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.AE_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.COUNT_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.DELTA_REPORT_DIR
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.IMAGING_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.NON_IMAGING_REPORT_NAME
import ac.uk.ebi.biostd.submission.stats.service.StatsReporterService.Companion.TOTAL_REPORT_DIR
import ebi.ac.uk.model.constants.TEXT_PLAIN
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Paths

@RestController
@RequestMapping("/stats/report")
@Tag(name = "Statistics Reports", description = "Generated report files for public counts and storage sizes.")
class StatsReportResource(
    private val appProperties: ApplicationProperties,
) {
    @Operation(summary = "Get total imaging storage size")
    @GetMapping("/imaging/size/total", produces = [TEXT_PLAIN])
    suspend fun getImagingTotalSize(): String = getReport("$TOTAL_REPORT_DIR/$IMAGING_REPORT_NAME")

    @Operation(summary = "Get imaging storage size delta")
    @GetMapping("/imaging/size/delta", produces = [TEXT_PLAIN])
    suspend fun getImagingDeltaSize(): String = getReport("$DELTA_REPORT_DIR/$IMAGING_REPORT_NAME")

    @Operation(summary = "Get imaging submission count")
    @GetMapping("/imaging/count", produces = [TEXT_PLAIN])
    suspend fun getImagingCount(): String = getReport("$COUNT_REPORT_DIR/$IMAGING_REPORT_NAME")

    @Operation(summary = "Get total non-imaging storage size")
    @GetMapping("/non-imaging/size/total", produces = [TEXT_PLAIN])
    suspend fun getNonImagingTotalSize(): String = getReport("$TOTAL_REPORT_DIR/$NON_IMAGING_REPORT_NAME")

    @Operation(summary = "Get non-imaging storage size delta")
    @GetMapping("/non-imaging/size/delta", produces = [TEXT_PLAIN])
    suspend fun getNonImagingDeltaSize(): String = getReport("$DELTA_REPORT_DIR/$NON_IMAGING_REPORT_NAME")

    @Operation(summary = "Get non-imaging submission count")
    @GetMapping("/non-imaging/count", produces = [TEXT_PLAIN])
    suspend fun getNonImagingCount(): String = getReport("$COUNT_REPORT_DIR/$NON_IMAGING_REPORT_NAME")

    @Operation(summary = "Get total ArrayExpress storage size")
    @GetMapping("/arrayexpress/size/total", produces = [TEXT_PLAIN])
    suspend fun getAETotalSize(): String = getReport("$TOTAL_REPORT_DIR/$AE_REPORT_NAME")

    @Operation(summary = "Get ArrayExpress storage size delta")
    @GetMapping("/arrayexpress/size/delta", produces = [TEXT_PLAIN])
    suspend fun getAEDeltaSize(): String = getReport("$DELTA_REPORT_DIR/$AE_REPORT_NAME")

    @Operation(summary = "Get ArrayExpress submission count")
    @GetMapping("/arrayexpress/count", produces = [TEXT_PLAIN])
    suspend fun getAECount(): String = getReport("$COUNT_REPORT_DIR/$AE_REPORT_NAME")

    private fun getReport(reportPath: String): String =
        Paths
            .get(appProperties.persistence.statsReportPath)
            .resolve(reportPath)
            .toFile()
            .readText()
}
