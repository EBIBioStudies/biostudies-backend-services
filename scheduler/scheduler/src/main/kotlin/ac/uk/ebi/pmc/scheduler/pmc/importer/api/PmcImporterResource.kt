package ac.uk.ebi.pmc.scheduler.pmc.importer.api

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
internal class PmcImporterResource(private val importerService: PmcLoaderService) {

    @PostMapping("/api/pmc/load/folder")
    fun loadFile(@RequestHeader("path") path: String) = importerService.loadFile(path)

    @PostMapping("/api/pmc/process")
    fun triggerProcessor() = importerService.triggerProcessor()

    @PostMapping("/api/pmc/submit")
    fun triggerSubmitter() = importerService.triggerSubmitter()
}
