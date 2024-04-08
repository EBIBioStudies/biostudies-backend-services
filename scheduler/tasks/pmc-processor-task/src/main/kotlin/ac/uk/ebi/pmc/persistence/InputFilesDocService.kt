package ac.uk.ebi.pmc.persistence

import ac.uk.ebi.pmc.load.FileSpec
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository
import java.io.File

class InputFilesDocService(private val inputFileRepo: InputFileRepository) {
    suspend fun reportProcessed(file: FileSpec) = inputFileRepo.saveProcessed(file)

    suspend fun reportFailed(
        file: File,
        error: String,
    ) = inputFileRepo.saveFailed(file, error)
}
