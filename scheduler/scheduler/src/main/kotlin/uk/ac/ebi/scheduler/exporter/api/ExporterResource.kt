package uk.ac.ebi.scheduler.exporter.api

import ac.uk.ebi.cluster.client.model.Job
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.scheduler.exporter.domain.ExporterTrigger

@RestController
internal class ExporterResource(private val exporterTrigger: ExporterTrigger) {
    @PostMapping("/api/exporter/public")
    @ResponseBody
    fun exportPublicSubmissions(): Job = exporterTrigger.triggerPublicExport()
}
