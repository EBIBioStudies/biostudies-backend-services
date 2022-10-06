package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission

class FileSystemService(
    private val storageService: FileStorageService,
) {
    fun cleanFolder(previousSubmission: ExtSubmission) {
        storageService.cleanSubmissionFiles(previousSubmission)
    }
}
