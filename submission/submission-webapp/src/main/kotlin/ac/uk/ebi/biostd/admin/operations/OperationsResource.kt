package ac.uk.ebi.biostd.admin.operations

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/operations")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Administration", description = "Internal maintenance jobs for request and temporary files.")
class OperationsResource(
    private val operationsService: OperationsService,
) {
    @Operation(summary = "Archive completed submission requests")
    @PostMapping("/archive-requests")
    suspend fun archiveRequest() {
        operationsService.archiveRequests()
    }

    @Operation(summary = "Archive one submission request")
    @PostMapping("/archive-requests/{accNo}/{version}")
    suspend fun archiveRequest(
        accNo: String,
        version: Int,
    ) {
        operationsService.archiveRequests(accNo, version)
    }

    @Operation(summary = "Delete completed request files")
    @PostMapping("/deleteRequestFiles")
    suspend fun deleteRequestFiles() {
        operationsService.deleteRequestFiles()
    }

    @Operation(summary = "Delete old temporary files")
    @PostMapping("/deleteTmpFiles")
    suspend fun deleteTempFiles() {
        operationsService.cleanTempFolders()
    }
}
