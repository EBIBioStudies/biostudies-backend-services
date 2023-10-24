package ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories

import ac.uk.ebi.biostd.persistence.doc.commons.pageResultAsFlow
import ac.uk.ebi.biostd.persistence.doc.db.repositories.MigrationData
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ExistsQuery
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

internal const val CHUNK_SIZE = 10

interface SubmissionMigratorRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ accNo: { \$regex: ?0 }, storageMode: 'NFS', version: { \$gte: 0 } }",
        fields = "{ accNo: 1 }"
    )
    fun findReadyToMigrate(accNoPattern: String, pageable: Pageable): Flow<MigrationData>

    @ExistsQuery(value = "{ accNo: ?0, storageMode: 'FIRE', version: { \$gte: 0 } }")
    fun isMigrated(accNo: String): Boolean
}

fun SubmissionMigratorRepository.getReadyToMigrate(
    accNoPattern: String
): Flow<MigrationData> = pageResultAsFlow { _, _ -> findReadyToMigrate(accNoPattern, PageRequest.of(0, CHUNK_SIZE)) }
