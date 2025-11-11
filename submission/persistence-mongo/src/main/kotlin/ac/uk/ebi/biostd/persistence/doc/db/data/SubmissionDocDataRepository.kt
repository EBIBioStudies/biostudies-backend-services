package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.ListFilter
import ac.uk.ebi.biostd.persistence.common.request.SimpleFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.commons.ExtendedUpdate
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionFile
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.LinkListDocLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
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
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

@Suppress("SpreadOperator", "TooManyFunctions")
class SubmissionDocDataRepository(
    private val submissionRepository: SubmissionMongoRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) : SubmissionMongoRepository by submissionRepository {
    suspend fun getCurrentMaxVersion(accNo: String): Int? {
        val aggregation =
            newAggregation(
                DocSubmission::class.java,
                match(where(SUB_ACC_NO).`is`(accNo)),
                group(SUB_ACC_NO).max(absoluteValueOf(SUB_VERSION)).`as`("maxVersion"),
                sort(Sort.Direction.DESC, "maxVersion"),
            )

        val result = mongoTemplate.aggregate(aggregation, Result::class.java).awaitFirstOrNull()
        return result?.maxVersion
    }

    suspend fun expireVersions(submissions: List<String>) {
        mongoTemplate
            .updateMulti(
                Query(where(SUB_ACC_NO).`in`(submissions).andOperator(where(SUB_VERSION).gt(0))),
                ExtendedUpdate().multiply(SUB_VERSION, -1),
                DocSubmission::class.java,
            ).awaitSingleOrNull()

        val innerFilesQuery =
            Query(
                where(DOC_SUB_FILE_SUBMISSION_ACC_NO)
                    .`in`(submissions)
                    .andOperator(where(DOC_SUB_FILE_SUBMISSION_VERSION).gt(0)),
            )
        mongoTemplate
            .updateMulti(
                innerFilesQuery,
                ExtendedUpdate().multiply(DOC_SUB_FILE_SUBMISSION_VERSION, -1),
                DocSubmissionFile::class.java,
            ).awaitSingleOrNull()

        val fileListQuery =
            Query(
                where(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO)
                    .`in`(submissions)
                    .andOperator(where(FILE_LIST_DOC_FILE_SUBMISSION_VERSION).gt(0)),
            )
        mongoTemplate
            .updateMulti(
                fileListQuery,
                ExtendedUpdate().multiply(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, -1),
                FileListDocFile::class.java,
            ).awaitSingleOrNull()

        val linkListQuery =
            Query(
                where(LINK_LIST_DOC_LINK_SUBMISSION_ACC_NO)
                    .`in`(submissions)
                    .andOperator(where(LINK_LIST_DOC_LINK_SUBMISSION_VERSION).gt(0)),
            )
        mongoTemplate
            .updateMulti(
                linkListQuery,
                ExtendedUpdate().multiply(LINK_LIST_DOC_LINK_SUBMISSION_VERSION, -1),
                LinkListDocLink::class.java,
            ).awaitSingleOrNull()
    }

    suspend fun getCollections(accNo: String): List<DocCollection> =
        submissionRepository.findSubmissionCollections(accNo)?.collections ?: emptyList()

    fun getSubmissions(filter: SubmissionFilter): Flow<DocSubmission> {
        val aggregations = createSubmissionAggregation(filter)
        val aggregation =
            newAggregation(
                DocSubmission::class.java,
                *aggregations.toTypedArray(),
            ).withOptions(aggregationOptions())

        return mongoTemplate.aggregate(aggregation, DocSubmission::class.java).asFlow()
    }

    suspend fun getSubmissionsPage(filter: SubmissionFilter): Page<DocSubmission> {
        val aggregation =
            newAggregation(
                DocSubmission::class.java,
                *createCountAggregation(filter).toTypedArray(),
            ).withOptions(aggregationOptions())

        val result = mongoTemplate.aggregate(aggregation, CountResult::class.java)
        return PageImpl(
            getSubmissions(filter).toList(),
            PageRequest.of(filter.pageNumber, filter.limit),
            result.awaitFirstOrNull()?.submissions ?: 0,
        )
    }

    suspend fun getSubmission(
        acc: String,
        version: Int,
    ): DocSubmission = submissionRepository.getByAccNoAndVersion(acc, version)

    companion object {
        private fun createCountAggregation(filter: SubmissionFilter) = createAggregation(filter).plus(group().count().`as`("submissions"))

        private fun createSubmissionAggregation(filter: SubmissionFilter) =
            createAggregation(filter, filter.offset to filter.limit.toLong())

        private fun aggregationOptions() = AggregationOptions.builder().allowDiskUse(true).build()

        private fun createAggregation(
            filter: SubmissionFilter,
            offsetLimit: Pair<Long, Long>? = null,
        ): List<AggregationOperation> =
            buildList {
                add(match(Criteria().andOperator(createQuery(filter))))
                add(sort(Sort.Direction.DESC, SUB_MODIFICATION_TIME))
                offsetLimit?.let {
                    add(skip(it.first))
                    add(limit(it.second))
                }
            }

        @Suppress("ComplexMethod")
        private fun createQuery(filter: SubmissionFilter): List<Criteria> {
            fun userFilter(filter: ListFilter): Criteria {
                val user = filter.filterUser
                val collections = filter.adminCollections

                return when (collections) {
                    null -> where(SUB_OWNER).`is`(user)
                    else ->
                        Criteria().orOperator(
                            where(SUB_OWNER).`is`(user),
                            where("$SUB_COLLECTIONS.$SUB_ACC_NO").`in`(collections),
                        )
                }
            }

            return buildList {
                when (filter) {
                    is ListFilter -> {
                        if (filter.findAnyAccNo.not()) add(userFilter(filter))
                        filter.accNo?.let { add(where(SUB_ACC_NO).`is`(it)) }
                        add(where(SUB_ACC_NO).nin(filter.notIncludeAccNo))
                    }

                    is SimpleFilter -> {
                        filter.rTimeFrom?.let { add(where(SUB_RELEASE_TIME).gte(it.toInstant())) }
                        filter.rTimeTo?.let { add(where(SUB_RELEASE_TIME).lte(it.toInstant())) }
                        filter.collection?.let { add(where("$SUB_COLLECTIONS.$SUB_ACC_NO").`in`(it)) }
                        filter.released?.let { add(where(SUB_RELEASED).`is`(it)) }
                    }
                }
            }
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
