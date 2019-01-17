package ac.uk.ebi.pmc.scheduler.pmc.importer

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.io.File

@RestController
class PmcImporterResource(private val importerService: PmcImporterService) {

    @GetMapping("/api/pmc/import/file")
    fun importFile(@RequestHeader("path") path: String) {
        importerService.importGzipFile(File(path))
    }

    @PostMapping("/api/pmc/import/files")
    fun importFolder(@RequestBody paths: List<String>) {
        importerService.importGzipFolder(paths.map(::File))
    }
}