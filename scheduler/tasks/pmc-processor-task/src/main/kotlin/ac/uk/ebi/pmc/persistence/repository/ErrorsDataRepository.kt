package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ErrorsDocRepository : CoroutineCrudRepository<SubmissionErrorDocument, ObjectId>

class ErrorsDataRepository(
    private val errorsDocRepository: ErrorsDocRepository,
) {
    suspend fun save(errorDoc: SubmissionErrorDocument): SubmissionErrorDocument = errorsDocRepository.save(errorDoc)

    suspend fun save(errorDoc: List<SubmissionErrorDocument>): List<SubmissionErrorDocument> =
        errorsDocRepository.saveAll(errorDoc).toList()
}
