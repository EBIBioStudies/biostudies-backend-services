package ac.uk.ebi.pmc.scheduler.pmc.importer.api

import ac.uk.ebi.pmc.scheduler.pmc.importer.DEFAULT_FOLDER
import ac.uk.ebi.pmc.scheduler.pmc.importer.domain.PmcLoaderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
internal class PmcImporterResource(private val pmcLoaderService: PmcLoaderService) {

    @PostMapping("/api/pmc/load/folder")
    fun loadFile(@RequestHeader(name = "path", defaultValue = DEFAULT_FOLDER) path: String) =
        pmcLoaderService.loadFile(path)

    @PostMapping("/api/pmc/process")
    fun triggerProcessor() = pmcLoaderService.triggerProcessor()

    @PostMapping("/api/pmc/submit")
    fun triggerSubmitter() = pmcLoaderService.triggerSubmitter()
}
