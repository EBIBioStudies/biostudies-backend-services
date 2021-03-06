package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.commons.ExtendedUpdate
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import com.google.common.collect.ImmutableList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.CURRENT
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.limit
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot
import org.springframework.data.mongodb.core.aggregation.Aggregation.skip
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.AggregationOptions
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update
import java.time.Instant

private const val SUB_ALIAS = "submission"

@Suppress("SpreadOperator")
class SubmissionDocDataRepository(
    private val submissionRepository: SubmissionMongoRepository,
    private val mongoTemplate: MongoTemplate
) : SubmissionMongoRepository by submissionRepository {
    fun updateStatus(status: DocProcessingStatus, accNo: String, version: Int) {
        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).`is`(version)))
        mongoTemplate.updateFirst(query, update(SUB_STATUS, status), DocSubmission::class.java)
    }

    fun getCurrentVersion(accNo: String): Int? {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            match(where(SUB_ACC_NO).`is`(accNo)),
            group(SUB_ACC_NO).max(SUB_VERSION).`as`("maxVersion"),
            sort(Sort.Direction.DESC, "maxVersion")
        )
        return mongoTemplate.aggregate(aggregation, Result::class.java).uniqueMappedResult?.maxVersion
    }

    fun getLatestVersions(accNo: List<String>, skip: Long, limit: Long): List<DocSubmission> {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            match(where(SUB_ACC_NO).`in`(accNo)),
            sort(Sort.Direction.DESC, SUB_VERSION),
            group(SUB_ACC_NO).first(SUB_VERSION).`as`("maxVersion").first(CURRENT).`as`("submission"),
            replaceRoot("submission"),
            skip(skip),
            limit(limit)
        )

        return mongoTemplate.aggregate(aggregation, DocSubmission::class.java).mappedResults
    }

    fun expireActiveProcessedVersions(accNo: String) {
        val criteria = where(SUB_ACC_NO).`is`(accNo).andOperator(
            where(SUB_VERSION).gt(0),
            where(SUB_STATUS).`is`(DocProcessingStatus.PROCESSED)
        )

        mongoTemplate.updateMulti(
            Query(criteria),
            ExtendedUpdate().multiply(SUB_VERSION, -1),
            DocSubmission::class.java
        )
    }

    fun expireVersion(accNo: String, version: Int) {
        mongoTemplate.updateMulti(
            Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).`is`(version))),
            ExtendedUpdate().multiply(SUB_VERSION, -1).set(SUB_MODIFICATION_TIME, Instant.now()),
            DocSubmission::class.java
        )
    }

    fun expireVersions(submissions: List<String>) {
        mongoTemplate.updateMulti(
            Query(where(SUB_ACC_NO).`in`(submissions).andOperator(where(SUB_VERSION).gt(0))),
            ExtendedUpdate().multiply(SUB_VERSION, -1).set(SUB_MODIFICATION_TIME, Instant.now()),
            DocSubmission::class.java
        )
    }

    fun getCollections(accNo: String): List<DocCollection> =
        submissionRepository.findSubmissionCollections(accNo)?.collections ?: emptyList()

    fun getSubmissions(filter: SubmissionFilter, email: String? = null): List<DocSubmission> {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            *createSubmissionAggregation(filter, email).toTypedArray()
        ).withOptions(aggregationOptions())

        return mongoTemplate.aggregate(aggregation, DocSubmission::class.java).mappedResults
    }

    fun getSubmissionsPage(filter: SubmissionFilter): Page<DocSubmission> {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            *createCountAggregation(filter).toTypedArray()
        ).withOptions(aggregationOptions())

        return PageImpl(
            getSubmissions(filter),
            PageRequest.of(filter.pageNumber, filter.limit),
            mongoTemplate.aggregate(aggregation, CountResult::class.java).uniqueMappedResult?.submissions ?: 0
        )
    }

    companion object {
        private fun createCountAggregation(filter: SubmissionFilter) =
            createAggregation(filter).plus(group().count().`as`("submissions"))

        private fun createSubmissionAggregation(filter: SubmissionFilter, email: String? = null) =
            createAggregation(filter, email).plus(skip(filter.offset)).plus(limit(filter.limit.toLong()))

        private fun aggregationOptions() = AggregationOptions.builder().allowDiskUse(true).build()

        private fun createAggregation(filter: SubmissionFilter, email: String? = null) =
            listOf(
                match(where(SUB_VERSION).gt(0).andOperator(*createQuery(filter, email))),
                sort(Sort.Direction.DESC, SUB_VERSION, SUB_ID),
                group(SUB_ACC_NO).first(SUB_VERSION).`as`("maxVersion").first(CURRENT).`as`(SUB_ALIAS),
                replaceRoot(SUB_ALIAS)
            )

        private fun createQuery(filter: SubmissionFilter, email: String? = null): Array<Criteria> =
            ImmutableList.Builder<Criteria>().apply {
                email?.let { add(where(SUB_OWNER).`is`(email)) }
                filter.accNo?.let { add(where(SUB_ACC_NO).`is`(it)) }
                filter.type?.let { add(where("$SUB_SECTION.$SEC_TYPE").`is`(it)) }
                filter.rTimeFrom?.let { add(where(SUB_RELEASE_TIME).gte(it.toInstant())) }
                filter.rTimeTo?.let { add(where(SUB_RELEASE_TIME).lte(it.toInstant())) }
                filter.keywords?.let { add(keywordsCriteria(it)) }
                filter.released?.let { add(where(SUB_RELEASED).`is`(it)) }
            }.build().toTypedArray()

        private fun keywordsCriteria(keywords: String) = Criteria().orOperator(
            where(SUB_TITLE).regex("(?i).*$keywords.*"),
            where("$SUB_SECTION.$SEC_ATTRIBUTES").elemMatch(
                where(ATTRIBUTE_DOC_NAME).`is`("Title").and(ATTRIBUTE_DOC_VALUE).regex("(?i).*$keywords.*")
            )
        )
    }
}

data class Result(
    val id: String,
    val maxVersion: Int
)

data class CountResult(
    val id: String?,
    val submissions: Long
)
