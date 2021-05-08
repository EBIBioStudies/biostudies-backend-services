package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update

class SubmissionRequestDocDataRepository(
    private val submissionRequestRepository: SubmissionRequestRepository,
    private val mongoTemplate: MongoTemplate
) : SubmissionRequestRepository by submissionRequestRepository {
    fun saveRequest(submissionRequest: SubmissionRequest) {
        submissionRequestRepository.save(submissionRequest)
    }

    fun getRequest(filter: SubmissionFilter, email: String? = null): List<SubmissionRequest> {
        val query = Query().limit(filter.limit).skip(filter.offset)
        query.addCriteria(createQuery(filter, email)).addCriteria(where("status").`is`(REQUESTED))
        return mongoTemplate.find(query, SubmissionRequest::class.java)
    }

    private fun createQuery(filter: SubmissionFilter, email: String? = null): Criteria {
        val criteria = where("submission.$SUB_OWNER").`is`(email)
        filter.accNo?.let { criteria.andOperator(where("submission.$SUB_ACC_NO").`is`(it)) }
        filter.type?.let { criteria.andOperator(where("submission.$SUB_SECTION.$SEC_TYPE").`is`(it)) }
        filter.rTimeFrom?.let { criteria.andOperator(where("submission.$SUB_RELEASE_TIME").gte(it.toString())) }
        filter.rTimeTo?.let { criteria.andOperator(where("submission.$SUB_RELEASE_TIME").lte(it.toString())) }
        filter.keywords?.let { criteria.andOperator(where("submission.$SUB_TITLE").regex("(?i).*$it.*")) }
        filter.released?.let { criteria.andOperator(where("submission.$SUB_RELEASED").`is`(it)) }
        return criteria
    }

    fun updateStatus(status: SubmissionRequestStatus, accNo: String) {
        val query = Query(where(SUB_ACC_NO).`is`(accNo))
        mongoTemplate.updateFirst(query, update("status", status), SubmissionRequest::class.java)
    }
}
