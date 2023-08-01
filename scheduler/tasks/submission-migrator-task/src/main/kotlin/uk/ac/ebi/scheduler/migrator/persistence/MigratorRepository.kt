package uk.ac.ebi.scheduler.migrator.persistence

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ExistsQuery
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository

internal const val CHUNK_SIZE = 10

interface MigratorRepository : PagingAndSortingRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ accNo: { \$regex: \"E-GEOD.*\" }, storageMode: 'NFS', version: { \$gte: 0 } }",
//        value = "{ accNo: { \$not: { \$regex: \"S-BIAD.*\" } }, storageMode: 'NFS', version: { \$gte: 0 }, \"section.type\": { \$ne: 'Project' } }",
        fields = "{ accNo: 1 }"
    )
    fun findReadyToMigrate(pageable: Pageable): Page<MigrationData>
    
    @ExistsQuery(value = "{ accNo: ?0, storageMode: 'FIRE', version: { \$gte: 0 } }")
    fun isMigrated(accNo: String): Boolean
}

// TODO this could be a common method in persistence-common
// TODO once done, all the iterators could be replaced
fun MigratorRepository.getReadyToMigrate(): Sequence<MigrationData> = sequence {
    var index = 0
    var currentPage = findReadyToMigrate(PageRequest.of(index, CHUNK_SIZE))

    while (currentPage.isEmpty.not()) {
        yieldAll(currentPage)
        currentPage = findReadyToMigrate(PageRequest.of(++index, CHUNK_SIZE))
    }
}

data class MigrationData(val accNo: String)
