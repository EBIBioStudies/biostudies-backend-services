package uk.ac.ebi.scheduler.pmc.exporter.persistence

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Meta
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData

internal const val PMC_COLLECTION = "S-EPMC"

interface PmcRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ 'collections.accNo': 'EuropePMC' , version: { \$gte: 0 } }",
        fields = "{ accNo: 1, title: 1 }",
    )
    @Meta(cursorBatchSize = 500)
    fun findAllPmc(): Flow<PmcData>
}
