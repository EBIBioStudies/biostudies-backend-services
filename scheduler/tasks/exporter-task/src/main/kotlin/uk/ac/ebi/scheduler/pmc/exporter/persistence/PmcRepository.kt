package uk.ac.ebi.scheduler.pmc.exporter.persistence

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData

internal const val PMC_COLLECTION = "S-EPMC"

interface PmcRepository : PagingAndSortingRepository<DocSubmission, ObjectId> {
    @Query(
        value = "{ accNo: { \$regex: \"$PMC_COLLECTION.*\" }, version: { \$gte: 0 } }",
        fields = "{ accNo: 1, title: 1 }"
    )
    fun findAllPmc(pageable: Pageable): Page<PmcData>
}
