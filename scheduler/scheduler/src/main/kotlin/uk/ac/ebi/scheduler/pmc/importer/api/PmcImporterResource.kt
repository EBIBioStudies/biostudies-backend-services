package uk.ac.ebi.scheduler.pmc.importer.api

import ac.uk.ebi.cluster.client.model.Job
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.scheduler.pmc.importer.domain.PmcLoaderService

@RestController
internal class PmcImporterResource(private val pmcLoaderService: PmcLoaderService) {
    @PostMapping("/api/pmc/load/folder")
    @ResponseBody
    fun loadFile(
        @RequestParam(required = false) debugPort: Int?,
        @RequestHeader(name = "folder", required = false) folder: String?,
        @RequestHeader(name = "file", required = false) file: String?,
    ): Job = pmcLoaderService.loadFile(folder, file, debugPort)

    @PostMapping("/api/pmc/process")
    @ResponseBody
    fun triggerProcessor(
        @RequestParam(required = false) debugPort: Int?,
        @RequestHeader(name = "sourceFile", required = false) sourceFile: String?,
    ): Job =
        pmcLoaderService.triggerProcessor(sourceFile, debugPort)

    @PostMapping("/api/pmc/submit")
    @ResponseBody
    fun triggerSubmitter(
        @RequestParam(required = false) debugPort: Int?,
        @RequestHeader(name = "sourceFile", required = false) sourceFile: String?,
    ): Job =
        pmcLoaderService.triggerSubmitter(sourceFile, debugPort)

    @PostMapping("/api/pmc/submit/{submissionId}")
    @ResponseBody
    fun triggerSingleSubmitter(
        @RequestParam(required = false) debugPort: Int?,
        @PathVariable submissionId: String,
    ): Job =
        pmcLoaderService.triggerSubmitSingle(debugPort, submissionId)
}
