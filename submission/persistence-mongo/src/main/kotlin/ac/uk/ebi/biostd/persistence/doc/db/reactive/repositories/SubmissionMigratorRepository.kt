package ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories

import ac.uk.ebi.biostd.persistence.doc.db.repositories.MigrationData
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ExistsQuery
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant

interface SubmissionMigratorRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ storageMode: 'NFS', modificationTime: {\$lte: ?0} , version: { \$gte: 0 }, released: true }",
        fields = "{ accNo: 1 }",
    )
    suspend fun findReadyToMigrate(before: Instant): Flow<MigrationData>

    @ExistsQuery(value = "{ accNo: ?0, storageMode: 'FIRE', version: { \$gte: 0 } }")
    suspend fun isMigrated(accNo: String): Boolean
}
