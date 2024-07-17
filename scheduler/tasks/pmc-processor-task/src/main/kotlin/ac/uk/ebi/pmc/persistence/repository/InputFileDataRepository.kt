package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.InputFileDocument
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface InputFilesDocRepository : CoroutineCrudRepository<InputFileDocument, ObjectId>

class InputFilesDataRepository(
    private val repository: InputFilesDocRepository,
) {
    suspend fun save(doc: InputFileDocument): InputFileDocument = repository.save(doc)
}
