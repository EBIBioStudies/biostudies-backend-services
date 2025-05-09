package uk.ac.ebi.scheduler.pmc.exporter.api

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.scheduler.pmc.exporter.domain.ExporterTrigger

@RestController
internal class ExporterResource(
    private val exporterTrigger: ExporterTrigger,
) {
    @PostMapping("/api/exporter/public")
    @ResponseBody
    suspend fun exportPublicSubmissions(
        @RequestParam(required = false) debugPort: Int?,
    ): Job = exporterTrigger.triggerPublicExport(debugPort)

    @PostMapping("/api/exporter/pmc")
    @ResponseBody
    suspend fun exportPmcSubmissions(
        @RequestParam(required = false) debugPort: Int?,
    ): Job = exporterTrigger.triggerPmcExport(debugPort)

    @PostMapping("/api/exporter/pmc/update-view")
    @ResponseBody
    suspend fun pmcViewUpdate(
        @RequestParam(required = false) debugPort: Int?,
    ): Job = exporterTrigger.triggerPmcViewUpdate(debugPort)
}
