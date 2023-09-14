package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SimpleFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.persistence.doc.commons.ExtendedUpdate
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.limit
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.skip
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationOptions
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Abs.absoluteValueOf
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.data.mongodb.core.query.Update.update
import java.time.Instant

@Suppress("SpreadOperator", "TooManyFunctions")
class SubmissionDocDataRepository(
    private val submissionRepository: SubmissionMongoRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) : SubmissionMongoRepository by submissionRepository {

    suspend fun saveSubmission(docSubmission: DocSubmission): DocSubmission {
        return submissionRepository.save(docSubmission).awaitSingle()
    }

    suspend fun saveAllSubmissions(submissions: List<DocSubmission>) {
        submissionRepository.saveAll(submissions).awaitFirst()
    }

    suspend fun deleteAllSubmissions() {
        submissionRepository.deleteAll().awaitSingleOrNull()
    }

    fun findAllSubmissions(): Flow<DocSubmission> {
        return submissionRepository.findAll().asFlow()
    }

    fun setAsReleased(accNo: String) {
        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).gt(0)))
        mongoTemplate.updateFirst(query, update(SUB_RELEASED, true), DocSubmission::class.java).block()
    }

    fun getCurrentMaxVersion(accNo: String): Int? {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            match(where(SUB_ACC_NO).`is`(accNo)),
            group(SUB_ACC_NO).max(absoluteValueOf(SUB_VERSION)).`as`("maxVersion"),
            sort(Sort.Direction.DESC, "maxVersion")
        )

        val result = mongoTemplate.aggregate(aggregation, Result::class.java).blockFirst()
        return result?.maxVersion
    }

    suspend fun expireVersions(submissions: List<String>) {
        mongoTemplate.updateMulti(
            Query(where(SUB_ACC_NO).`in`(submissions).andOperator(where(SUB_VERSION).gt(0))),
            ExtendedUpdate().multiply(SUB_VERSION, -1).set(SUB_MODIFICATION_TIME, Instant.now()),
            DocSubmission::class.java
        ).awaitSingleOrNull()

        val fileListQuery = Query(
            where(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO).`in`(submissions)
                .andOperator(where(FILE_LIST_DOC_FILE_SUBMISSION_VERSION).gt(0))
        )
        mongoTemplate.updateMulti(
            fileListQuery,
            ExtendedUpdate()
                .multiply(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, -1)
                .set(SUB_MODIFICATION_TIME, Instant.now()),
            FileListDocFile::class.java
        ).awaitSingleOrNull()
    }

    suspend fun getCollections(accNo: String): List<DocCollection> {
        return submissionRepository.findSubmissionCollections(accNo)?.collections ?: emptyList()
    }

    fun getSubmissions(filter: SubmissionFilter): List<DocSubmission> {
        val aggregations = createSubmissionAggregation(filter)
        val aggregation = newAggregation(
            DocSubmission::class.java,
            *aggregations.toTypedArray()
        ).withOptions(aggregationOptions())

        return mongoTemplate.aggregate(aggregation, DocSubmission::class.java)
            .collectList()
            .block()!!
    }

    fun getSubmissionsPage(filter: SubmissionFilter): Page<DocSubmission> {
        val aggregation = newAggregation(
            DocSubmission::class.java,
            *createCountAggregation(filter).toTypedArray()
        ).withOptions(aggregationOptions())

        val result = mongoTemplate.aggregate(aggregation, CountResult::class.java)
        return PageImpl(
            getSubmissions(filter),
            PageRequest.of(filter.pageNumber, filter.limit),
            result.blockFirst()?.submissions ?: 0
        )
    }

    suspend fun getSubmission(acc: String, version: Int): DocSubmission =
        submissionRepository.getByAccNoAndVersion(acc, version)

    companion object {
        private fun createCountAggregation(filter: SubmissionFilter) =
            createAggregation(filter).plus(group().count().`as`("submissions"))

        private fun createSubmissionAggregation(filter: SubmissionFilter) =
            createAggregation(filter, filter.offset to filter.limit.toLong())

        private fun aggregationOptions() = AggregationOptions.builder().allowDiskUse(true).build()

        private fun createAggregation(
            filter: SubmissionFilter,
            offsetLimit: Pair<Long, Long>? = null,
        ): List<AggregationOperation> = buildList {
            addAll(createQuery(filter))
            add(sort(Sort.Direction.DESC, SUB_MODIFICATION_TIME))
            offsetLimit?.let { add(skip(it.first)); add(limit(it.second)) }
        }

        private fun createQuery(filter: SubmissionFilter): List<MatchOperation> {
            return buildList {
                when (filter) {
                    is SimpleFilter -> {}
                    is SubmissionListFilter -> {
                        filter.keywords?.let { add(match(keywordsCriteria(it))) }
                        filter.type?.let { add(match(where("$SUB_SECTION.$SEC_TYPE").`is`(it))) }
                        add(match(where(SUB_VERSION).gt(0)))

                        if (filter.findAnyAccNo && filter.accNo != null) {
                            add(match(where(SUB_ACC_NO).`is`(filter.accNo)))
                        } else {
                            add(match(where(SUB_OWNER).`is`(filter.filterUser)))
                            filter.accNo?.let { add(match(where(SUB_ACC_NO).`is`(it))) }
                        }
                    }
                }
                filter.notIncludeAccNo?.let { add(match(where(SUB_ACC_NO).nin(it))) }
                filter.rTimeFrom?.let { add(match(where(SUB_RELEASE_TIME).gte(it.toInstant()))) }
                filter.rTimeTo?.let { add(match(where(SUB_RELEASE_TIME).lte(it.toInstant()))) }
                filter.collection?.let { add(match(where("$SUB_COLLECTIONS.$SUB_ACC_NO").`in`(it))) }
                filter.released?.let { add(match(where(SUB_RELEASED).`is`(it))) }
            }
        }

        private fun keywordsCriteria(keywords: String): TextCriteria {
            val terms = keywords.split("\\s").map { "\"$it\"" }.toTypedArray()
            return TextCriteria.forDefaultLanguage()
                .matchingAny(*terms)
                .caseSensitive(false)
        }
    }
}

data class Result(
    val id: String,
    val maxVersion: Int,
)

data class CountResult(
    val id: String?,
    val submissions: Long,
)
