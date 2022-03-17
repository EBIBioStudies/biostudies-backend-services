package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import com.google.common.collect.ImmutableList
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update

class SubmissionRequestDocDataRepository(
    private val submissionRequestRepository: SubmissionRequestRepository,
    private val mongoTemplate: MongoTemplate
) : SubmissionRequestRepository by submissionRequestRepository {
    fun saveRequest(submissionRequest: DocSubmissionRequest): DocSubmissionRequest =
        submissionRequestRepository.save(submissionRequest)

    fun findActiveRequest(filter: SubmissionFilter, email: String? = null): Pair<Int, List<DocSubmissionRequest>> {
        val query = Query().addCriteria(createQuery(filter, email))
        val requestCount = mongoTemplate.count(query, DocSubmissionRequest::class.java)
        return when {
            requestCount <= filter.offset -> requestCount.toInt() to emptyList()
            else -> findActiveRequest(query, filter.offset, filter.limit)
        }
    }

    private fun findActiveRequest(
        query: Query,
        skip: Long,
        limit: Int
    ): Pair<Int, MutableList<DocSubmissionRequest>> {
        val result = mongoTemplate.find(query.skip(skip).limit(limit), DocSubmissionRequest::class.java)
        return result.count() to result
    }

    @Suppress("SpreadOperator")
    private fun createQuery(filter: SubmissionFilter, email: String? = null): Criteria =
        where("submission.$SUB_OWNER").`is`(email)
            .and("status").`is`(REQUESTED)
            .andOperator(*criteriaArray(filter))

    fun updateStatus(status: SubmissionRequestStatus, accNo: String, version: Int) {
        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).`is`(version)))
        mongoTemplate.updateFirst(query, update("status", status), DocSubmissionRequest::class.java)
    }

    private fun criteriaArray(filter: SubmissionFilter): Array<Criteria> =
        ImmutableList.Builder<Criteria>().apply {
            add(where("status").`is`(REQUESTED))
            filter.accNo?.let { add(where("submission.$SUB_ACC_NO").`is`(it)) }
            filter.type?.let { add(where("submission.$SUB_SECTION.$SEC_TYPE").`is`(it)) }
            filter.rTimeFrom?.let { add(where("submission.$SUB_RELEASE_TIME").gte(it.toString())) }
            filter.rTimeTo?.let { add(where("submission.$SUB_RELEASE_TIME").lte(it.toString())) }
            filter.keywords?.let { add(keywordsCriteria(it)) }
            filter.released?.let { add(where("submission.$SUB_RELEASED").`is`(it)) }
        }.build().toTypedArray()

    private fun keywordsCriteria(keywords: String) = Criteria().orOperator(
        where("submission.$SUB_TITLE").regex("(?i).*$keywords.*"),
        where("submission.$SUB_SECTION.$SEC_ATTRIBUTES").elemMatch(
            where(ATTRIBUTE_DOC_NAME).`is`("Title").and(ATTRIBUTE_DOC_VALUE).regex("(?i).*$keywords.*")
        )
    )
}
