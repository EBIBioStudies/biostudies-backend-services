package uk.ac.ebi.scheduler.releaser.persistence

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import uk.ac.ebi.scheduler.releaser.model.ReleaseData
import java.util.Date

interface ReleaserRepository : PagingAndSortingRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ released: true, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, owner: 1, relPath: 1 }"
    )
    fun findAllReleased(): List<ReleaseData>

    @Query(
        value = "{ releaseTime: { \$lte: ?0 }, released: false, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, owner: 1, relPath: 1 }"
    )
    fun findAllUntil(toRTime: Date): List<ReleaseData>

    @Query(
        value = "{ releaseTime: { \$gte: ?0, \$lte: ?1 }, released: false, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, owner: 1, relPath: 1 }"
    )
    fun findAllBetween(fromRTime: Date, toRTime: Date): List<ReleaseData>
}
