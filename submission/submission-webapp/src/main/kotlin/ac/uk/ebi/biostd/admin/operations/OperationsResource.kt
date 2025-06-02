package ac.uk.ebi.biostd.admin.operations

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/operations")
@PreAuthorize("isAuthenticated()")
class OperationsResource(
    private val operationsService: OperationsService,
) {
    @PostMapping("/archive-requests")
    suspend fun archiveRequest() {
        operationsService.archiveRequests()
    }

    @PostMapping("/archive-requests/{accNo}/{version}")
    suspend fun archiveRequest(
        accNo: String,
        version: Int,
    ) {
        operationsService.archiveRequests(accNo, version)
    }

    @PostMapping("/deleteRequestFiles")
    suspend fun deleteRequestFiles() {
        operationsService.deleteRequestFiles()
    }

    @PostMapping("/deleteTmpFiles")
    suspend fun deleteTempFiles() {
        operationsService.cleanTempFolders()
    }
}
