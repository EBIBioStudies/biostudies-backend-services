package uk.ac.ebi.scheduler.pmc.exporter.api

import ac.uk.ebi.cluster.client.model.Job
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.scheduler.pmc.exporter.domain.PmcExporterTrigger

@RestController
internal class PmcExporterResource(private val pmcExporterTrigger: PmcExporterTrigger) {
    @PostMapping("/api/pmc/export")
    @ResponseBody
    fun exportPmcLinks(): Job = pmcExporterTrigger.triggerPmcExport()
}
