package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update

@Suppress("SpreadOperator")
class SubmissionRequestDocDataRepository(
    private val submissionRequestRepository: SubmissionRequestRepository,
    private val mongoTemplate: MongoTemplate
) : SubmissionRequestRepository by submissionRequestRepository {
    fun saveRequest(submissionRequest: SubmissionRequest) {
        submissionRequestRepository.save(submissionRequest)
    }

    fun getRequest(filter: SubmissionFilter, email: String? = null): List<SubmissionRequest> {
        val query = Query().limit(filter.limit).skip(filter.offset)
        query.addCriteria(createQuery(filter, email))
        return mongoTemplate.find(query, SubmissionRequest::class.java)
    }

    private fun createQuery(filter: SubmissionFilter, email: String? = null): Criteria =
        where("submission.$SUB_OWNER").`is`(email).andOperator(*criteriaArray(filter))

    fun updateStatus(status: SubmissionRequestStatus, accNo: String, version: Int) {
        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).`is`(version)))
        mongoTemplate.updateFirst(query, update("status", status), SubmissionRequest::class.java)
    }

    private fun criteriaArray(filter: SubmissionFilter): Array<Criteria> {
        val array = mutableListOf<Criteria>()
        array.add(where("status").`is`(REQUESTED))
        filter.accNo?.let { array.add(where("submission.$SUB_ACC_NO").`is`(it)) }
        filter.type?.let { array.add(where("submission.$SUB_SECTION.$SEC_TYPE").`is`(it)) }
        filter.rTimeFrom?.let { array.add(where("submission.$SUB_RELEASE_TIME").gte(it.toString())) }
        filter.rTimeTo?.let { array.add(where("submission.$SUB_RELEASE_TIME").lte(it.toString())) }
        filter.keywords?.let { array.add(where("submission.$SUB_TITLE").regex("(?i).*$it.*")) }
        filter.released?.let { array.add(where("submission.$SUB_RELEASED").`is`(it)) }
        return array.toTypedArray()
    }
}
