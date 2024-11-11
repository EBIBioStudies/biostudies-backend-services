package uk.ac.ebi.scheduler.pmc.exporter.persistence

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData

internal const val PMC_COLLECTION = "S-EPMC"

interface PmcRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ accNo: { \$regex: \"$PMC_COLLECTION.*\" }, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, title: 1 }",
        sort = "{ accNo: \"ASC\" }",
    )
    fun findAllPmc(): Flow<PmcData>
}
