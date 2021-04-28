package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

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

    private fun createQuery(filter: SubmissionFilter, email: String? = null): Criteria {
        val criteria = where("submission.${DocSubmissionFields.SUB_OWNER}").`is`(email)
        filter.accNo?.let { criteria.andOperator(where("submission.${DocSubmissionFields.SUB_ACC_NO}").`is`(it)) }
        return criteria
    }

}
