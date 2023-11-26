package ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories

import ac.uk.ebi.biostd.persistence.doc.db.repositories.ReleaseData
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.Date

interface SubmissionReleaserRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ released: true, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, owner: 1, relPath: 1 }"
    )
    fun findAllReleased(): Flow<ReleaseData>

    @Query(
        value = "{ releaseTime: { \$lte: ?0 }, released: false, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, owner: 1, relPath: 1 }"
    )
    fun findAllUntil(toRTime: Date): Flow<ReleaseData>

    @Query(
        value = "{ releaseTime: { \$gte: ?0, \$lte: ?1 }, released: false, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, owner: 1, relPath: 1 }"
    )
    fun findAllBetween(fromRTime: Date, toRTime: Date): Flow<ReleaseData>
}
