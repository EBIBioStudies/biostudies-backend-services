package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubFileDocument
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SubFileDocRepository : CoroutineCrudRepository<SubFileDocument, ObjectId>

class SubFileDataRepository(
    private val repository: SubFileDocRepository,
) {
    suspend fun save(errorDoc: SubFileDocument): SubFileDocument = repository.save(errorDoc)

    fun findByIds(files: List<ObjectId>): Flow<SubFileDocument> {
        return repository.findAllById(files)
    }
}
