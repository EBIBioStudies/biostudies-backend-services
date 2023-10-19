package uk.ac.ebi.scheduler.pmc.exporter.persistence

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ID
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ebi.ac.uk.coroutines.allPagesAsFlow
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData

internal const val PMC_COLLECTION = "S-EPMC"

interface PmcRepository : CoroutineCrudRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ accNo: { \$regex: \"$PMC_COLLECTION.*\" }, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, title: 1 }"
    )
    fun findAll(pageable: Pageable): Flow<PmcData>

    fun findAllSubmissions(): Flow<PmcData> {
        return allPagesAsFlow() { page, limit -> findAll(PageRequest.of(page, limit, Sort.by(ASC, SUB_ID))) }
    }
}

