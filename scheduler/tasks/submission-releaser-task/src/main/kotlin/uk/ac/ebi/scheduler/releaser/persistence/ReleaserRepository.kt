package uk.ac.ebi.scheduler.releaser.persistence

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import uk.ac.ebi.scheduler.releaser.model.ReleaseData
import java.util.Date

interface ReleaserRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
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
