package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionRequestNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.model.action
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_DRAFT
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_ERRORS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_FILE_CHANGES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_IDX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_PREV_SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_PROCESS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS_CHANGES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS_CHANGE_END_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS_CHANGE_RESULT
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS_CHANGE_STATUS_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_TOTAL_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_PREVIOUS_SUB_FILE
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_RQT
import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_RQT_ARCHIVE
import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_RQT_FILES
import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_RQT_FILES_ARCHIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocRequestStatusChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import com.google.common.collect.ImmutableList
import com.mongodb.BasicDBObject
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.Companion.ACTIVE_STATUS
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSED_STATUS
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.AggregationOptions
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.aggregation.MergeOperation.WhenDocumentsDontMatch
import org.springframework.data.mongodb.core.aggregation.MergeOperation.WhenDocumentsMatch
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Update.update
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.Instant

@Suppress("TooManyFunctions")
class SubmissionRequestDocDataRepository(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val extSerializationService: ExtSerializationService,
    private val submissionRequestRepository: SubmissionRequestRepository,
) : SubmissionRequestRepository by submissionRequestRepository {
    suspend fun saveRequest(request: DocSubmissionRequest): DocSubmissionRequest {
        val query =
            Query(
                where(RQT_ACC_NO)
                    .`is`(request.accNo)
                    .andOperator(where(RQT_STATUS).nin(PROCESSED_STATUS)),
            )

        mongoTemplate
            .upsert(
                query,
                request.asUpsert(),
                DocSubmissionRequest::class.java,
            ).awaitSingle()

        return submissionRequestRepository.getByAccNoAndStatusNotIn(request.accNo, PROCESSED_STATUS)
    }

    /**
     * Archive the given request. Note that {@see Document} is used rathet than specific entity type to avoid schema
     * changes to affect operation.
     */
    suspend fun archiveRequest(
        accNo: String,
        version: Int,
    ): Long {
        var criteria =
            where(RQT_FILE_SUB_ACC_NO)
                .`is`(accNo)
                .andOperator(where(RQT_FILE_SUB_VERSION).`is`(version))

        suspend fun archiveRequestFiles(): Long {
            var mergeOperation =
                Aggregation
                    .merge()
                    .intoCollection(SUB_RQT_FILES_ARCHIVE)
                    .on(Fields.UNDERSCORE_ID)
                    .whenMatched(WhenDocumentsMatch.replaceDocument())
                    .whenNotMatched(WhenDocumentsDontMatch.insertNewDocument())
                    .build()
            val aggregation =
                Aggregation
                    .newAggregation(
                        Document::class.java,
                        match(criteria),
                        mergeOperation,
                    ).withOptions(
                        AggregationOptions
                            .builder()
                            .allowDiskUse(true)
                            .skipOutput()
                            .build(),
                    )
            mongoTemplate
                .aggregate(aggregation, SUB_RQT_FILES, Document::class.java)
                .awaitFirstOrNull()
            return mongoTemplate.count(Query().addCriteria(criteria), SUB_RQT_FILES_ARCHIVE).awaitSingle()
        }

        suspend fun archiveRequest() {
            var mergeOperation =
                Aggregation
                    .merge()
                    .intoCollection(SUB_RQT_ARCHIVE)
                    .on(Fields.UNDERSCORE_ID)
                    .whenMatched(WhenDocumentsMatch.replaceDocument())
                    .whenNotMatched(WhenDocumentsDontMatch.insertNewDocument())
                    .build()
            val aggregation =
                Aggregation
                    .newAggregation(
                        Document::class.java,
                        match(criteria),
                        mergeOperation,
                    ).withOptions(
                        AggregationOptions
                            .builder()
                            .allowDiskUse(true)
                            .skipOutput()
                            .build(),
                    )
            mongoTemplate
                .aggregate(aggregation, SUB_RQT, Document::class.java)
                .awaitFirstOrNull()
        }

        val archivedFiles = archiveRequestFiles()
        archiveRequest()
        return archivedFiles
    }

    suspend fun findActiveRequests(filter: SubmissionListFilter): Pair<Int, List<DocSubmissionRequest>> {
        val query = Query().addCriteria(createQuery(filter))
        val requestCount = mongoTemplate.count(query, DocSubmissionRequest::class.java).awaitSingle()
        return when {
            requestCount <= filter.offset -> requestCount.toInt() to emptyList()
            else -> findActiveRequests(query, filter.offset, filter.limit)
        }
    }

    suspend fun getRequest(
        accNo: String,
        version: Int,
    ): DocSubmissionRequest =
        submissionRequestRepository
            .findByAccNoAndVersion(accNo, version)
            ?: throw SubmissionRequestNotFoundException(accNo, version)

    suspend fun getRequest(
        accNo: String,
        version: Int,
        status: RequestStatus,
        processId: String,
    ): Pair<String, DocSubmissionRequest> {
        val statusId = ObjectId()
        val statusChange =
            DocRequestStatusChanges(
                status = status.action,
                statusId = statusId,
                processId = processId,
                startTime = Instant.now(),
                endTime = null,
                result = null,
            )
        val update = Update().addToSet("$RQT_PROCESS.$RQT_STATUS_CHANGES", statusChange)
        val query =
            Query(
                where(RQT_ACC_NO)
                    .`is`(accNo)
                    .and(RQT_VERSION)
                    .`is`(version)
                    .and(RQT_STATUS)
                    .`is`(status),
            )
        val result =
            mongoTemplate
                .findAndModify(
                    query,
                    update,
                    FindAndModifyOptions.options().returnNew(true),
                    DocSubmissionRequest::class.java,
                ).awaitSingle()
        return statusId.toString() to result
    }

    private suspend fun findActiveRequests(
        query: Query,
        skip: Long,
        limit: Int,
    ): Pair<Int, List<DocSubmissionRequest>> {
        val result =
            mongoTemplate
                .find(query.skip(skip).limit(limit), DocSubmissionRequest::class.java)
                .asFlow()
                .toList()
        return result.count() to result
    }

    @Suppress("SpreadOperator")
    private fun createQuery(filter: SubmissionListFilter): Criteria =
        where("$RQT_PROCESS.$SUB.$SUB_OWNER")
            .`is`(filter.filterUser)
            .andOperator(*criteriaArray(filter))

    suspend fun increaseIndex(
        accNo: String,
        version: Int,
    ) {
        val update = Update().inc("$RQT_PROCESS.$RQT_IDX", 1).currentDate(RQT_MODIFICATION_TIME)
        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).`is`(version)))
        mongoTemplate.updateFirst(query, update, DocSubmissionRequest::class.java).awaitSingleOrNull()
    }

    suspend fun upsertSubRqtFile(file: SubmissionRequestFile) {
        val serializedFile = BasicDBObject.parse(extSerializationService.serialize(file.file))
        val update =
            update(RQT_FILE_FILE, serializedFile)
                .set(RQT_FILE_INDEX, file.index)
                .set(RQT_FILE_STATUS, file.status)
        val where =
            where(RQT_FILE_SUB_ACC_NO)
                .`is`(file.accNo)
                .andOperator(
                    where(RQT_FILE_SUB_VERSION).`is`(file.version),
                    where(RQT_FILE_PATH).`is`(file.path),
                    where(RQT_PREVIOUS_SUB_FILE).`is`(file.previousSubFile),
                )

        mongoTemplate.upsert(Query(where), update, DocSubmissionRequestFile::class.java).awaitSingleOrNull()
    }

    suspend fun updateSubRqtFile(file: SubmissionRequestFile) {
        val serializedFile = extSerializationService.serialize(file.file)
        val update = update(RQT_FILE_FILE, BasicDBObject.parse(serializedFile)).set(RQT_FILE_STATUS, file.status)
        val where =
            where(RQT_FILE_SUB_ACC_NO)
                .`is`(file.accNo)
                .andOperator(
                    where(RQT_FILE_SUB_VERSION).`is`(file.version),
                    where(RQT_FILE_PATH).`is`(file.path),
                    where(RQT_PREVIOUS_SUB_FILE).`is`(file.previousSubFile),
                )
        mongoTemplate.updateFirst(Query(where), update, DocSubmissionRequestFile::class.java).awaitSingleOrNull()
    }

    suspend fun updateRqtDraft(
        accNo: String,
        owner: String,
        draft: String,
        modificationTime: Instant,
    ) {
        val update = update(RQT_DRAFT, draft).set(RQT_MODIFICATION_TIME, modificationTime)
        val where =
            where(RQT_ACC_NO)
                .`is`(accNo)
                .andOperator(
                    where(RQT_OWNER).`is`(owner),
                    where(RQT_STATUS).nin(PROCESSED_STATUS),
                )

        mongoTemplate.updateFirst(Query(where), update, DocSubmissionRequest::class.java).awaitSingleOrNull()
    }

    suspend fun setSubRequestAccNo(
        tempAccNo: String,
        accNo: String,
        owner: String,
        modificationTime: Instant,
    ) {
        val update = update(RQT_ACC_NO, accNo).set(RQT_MODIFICATION_TIME, modificationTime)
        val where =
            where(RQT_ACC_NO)
                .`is`(tempAccNo)
                .andOperator(where(RQT_OWNER).`is`(owner), where(RQT_STATUS).nin(PROCESSED_STATUS))

        mongoTemplate.updateFirst(Query(where), update, DocSubmissionRequest::class.java).awaitSingleOrNull()
    }

    suspend fun setSubRequestErrors(
        accNo: String,
        owner: String,
        errors: List<String>,
        modificationTime: Instant,
    ) {
        val update = update(RQT_ERRORS, errors).set(RQT_MODIFICATION_TIME, modificationTime)
        val where =
            where(RQT_ACC_NO)
                .`is`(accNo)
                .andOperator(where(RQT_OWNER).`is`(owner), where(RQT_STATUS).nin(PROCESSED_STATUS))

        mongoTemplate.updateFirst(Query(where), update, DocSubmissionRequest::class.java).awaitSingleOrNull()
    }

    suspend fun setRequestDraftStatus(
        accNo: String,
        owner: String,
        status: RequestStatus,
        modificationTime: Instant,
    ) {
        val update = update(RQT_STATUS, status).set(RQT_MODIFICATION_TIME, modificationTime)
        val where =
            where(RQT_ACC_NO)
                .`is`(accNo)
                .andOperator(
                    where(RQT_OWNER).`is`(owner),
                    where(RQT_STATUS).nin(PROCESSED_STATUS),
                )
        mongoTemplate.updateFirst(Query(where), update, DocSubmissionRequest::class.java).awaitSingleOrNull()
    }

    suspend fun updateSubmissionRequest(
        rqt: DocSubmissionRequest,
        processId: String,
        processEndTime: Instant,
        processResult: ProcessResult,
    ) {
        val query =
            Query(
                where(SUB_ACC_NO)
                    .`is`(rqt.accNo)
                    .and(SUB_VERSION)
                    .`is`(rqt.version)
                    .and("$RQT_PROCESS.$RQT_STATUS_CHANGES.$RQT_STATUS_CHANGE_STATUS_ID")
                    .`is`(ObjectId(processId)),
            )
        val update =
            Update()
                .set(RQT_STATUS, rqt.status)
                .set(RQT_MODIFICATION_TIME, rqt.modificationTime)
                .set(RQT_ERRORS, rqt.errors)
                .set("$RQT_PROCESS.$SUB", rqt.process?.submission)
                .set("$RQT_PROCESS.$RQT_TOTAL_FILES", rqt.process?.totalFiles)
                .set("$RQT_PROCESS.$RQT_IDX", rqt.process?.currentIndex)
                .set("$RQT_PROCESS.$RQT_TOTAL_FILES", rqt.process?.totalFiles)
                .set("$RQT_PROCESS.$RQT_FILE_CHANGES", rqt.process?.fileChanges)
                .set("$RQT_PROCESS.$RQT_PREV_SUB_VERSION", rqt.process?.previousVersion)
                .set("$RQT_PROCESS.$RQT_STATUS_CHANGES.$.$RQT_STATUS_CHANGE_END_TIME", processEndTime)
                .set("$RQT_PROCESS.$RQT_STATUS_CHANGES.$.$RQT_STATUS_CHANGE_RESULT", processResult.toString())
        mongoTemplate.updateFirst(query, update, DocSubmissionRequest::class.java).awaitSingleOrNull()
    }

    private fun criteriaArray(filter: SubmissionListFilter): Array<Criteria> =
        ImmutableList
            .Builder<Criteria>()
            .apply {
                add(where(RQT_STATUS).`in`(ACTIVE_STATUS))
                filter.accNo?.let { add(where("$RQT_PROCESS.$SUB.$SUB_ACC_NO").`is`(it)) }
                filter.type?.let { add(where("$RQT_PROCESS.$SUB.$SUB_SECTION.$SEC_TYPE").`is`(it)) }
                filter.rTimeFrom?.let { add(where("$RQT_PROCESS.$SUB.$SUB_RELEASE_TIME").gte(it.toString())) }
                filter.rTimeTo?.let { add(where("$RQT_PROCESS.$SUB.$SUB_RELEASE_TIME").lte(it.toString())) }
                filter.keywords?.let { add(keywordsCriteria(it)) }
                filter.released?.let { add(where("$RQT_PROCESS.$SUB.$SUB_RELEASED").`is`(it)) }
            }.build()
            .toTypedArray()

    private fun keywordsCriteria(keywords: String) =
        Criteria().orOperator(
            where("$RQT_PROCESS.$SUB.$SUB_TITLE").regex("(?i).*$keywords.*"),
            where("$RQT_PROCESS.$SUB.$SUB_SECTION.$SEC_ATTRIBUTES").elemMatch(
                where(ATTRIBUTE_DOC_NAME).`is`("Title").and(ATTRIBUTE_DOC_VALUE).regex("(?i).*$keywords.*"),
            ),
        )
}

enum class ProcessResult {
    SUCCESS,
    ERROR,
}
