package ac.uk.ebi.pmc.persistence

import ac.uk.ebi.pmc.load.FileSpec
import ac.uk.ebi.pmc.persistence.repository.InputFileRepository

class InputFilesDocService(private val inputFileRepo: InputFileRepository) {

    suspend fun isProcessed(file: FileSpec) = inputFileRepo.find(file).isDefined()

    suspend fun reportProcessed(file: FileSpec) = inputFileRepo.save(file)
}
