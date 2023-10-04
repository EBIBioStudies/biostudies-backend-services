package uk.ac.ebi.scheduler.migrator.persistence

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ExistsQuery
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

internal const val CHUNK_SIZE = 10

interface MigratorRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ accNo: { \$regex: ?0 }, storageMode: 'NFS', version: { \$gte: 0 } }",
        fields = "{ accNo: 1 }"
    )
    fun findReadyToMigrate(accNoPattern: String, pageable: Pageable): Flow<MigrationData>
    
    @ExistsQuery(value = "{ accNo: ?0, storageMode: 'FIRE', version: { \$gte: 0 } }")
    fun isMigrated(accNo: String): Boolean
}

fun MigratorRepository.getReadyToMigrate(accNoPattern: String): Flow<MigrationData> = flow {
    var index = 0
    var currentPage = findReadyToMigrate(accNoPattern, PageRequest.of(index, CHUNK_SIZE)).toList()

    while (currentPage.isNotEmpty()) {
        currentPage.forEach { emit(it) }
        currentPage = findReadyToMigrate(accNoPattern, PageRequest.of(++index, CHUNK_SIZE)).toList()
    }
}

data class MigrationData(val accNo: String)
