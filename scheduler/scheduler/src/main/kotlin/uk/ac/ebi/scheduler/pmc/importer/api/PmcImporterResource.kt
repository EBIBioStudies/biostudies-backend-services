package uk.ac.ebi.scheduler.pmc.importer.api

import ac.uk.ebi.cluster.client.model.Job
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService

@RestController
internal class PmcImporterResource(private val pmcLoaderService: PmcLoaderService) {
    @PostMapping("/api/pmc/load/folder")
    @ResponseBody
    fun loadFile(@RequestHeader(name = "path", required = false) path: String?): Job = pmcLoaderService.loadFile(path)

    @PostMapping("/api/pmc/process")
    @ResponseBody
    fun triggerProcessor(): Job = pmcLoaderService.triggerProcessor()

    @PostMapping("/api/pmc/submit")
    @ResponseBody
    fun triggerSubmitter(): Job = pmcLoaderService.triggerSubmitter()
}
