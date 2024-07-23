package ac.uk.ebi.pmc.persistence.domain

import ac.uk.ebi.pmc.load.FileSpec
import ac.uk.ebi.pmc.persistence.docs.InputFileDocument
import ac.uk.ebi.pmc.persistence.docs.InputFileStatus
import ac.uk.ebi.pmc.persistence.repository.InputFilesDataRepository
import java.io.File

class InputFilesService(private val inputFileRepo: InputFilesDataRepository) {
    suspend fun reportProcessed(file: FileSpec) {
        val doc =
            InputFileDocument(
                name = file.name,
                status = InputFileStatus.PROCESSED,
                error = null,
            )
        inputFileRepo.save(doc)
    }

    suspend fun reportFailed(
        file: File,
        error: String,
    ) {
        val doc =
            InputFileDocument(
                name = file.name,
                status = InputFileStatus.FAILED,
                error = error,
            )
        inputFileRepo.save(doc)
    }
}
