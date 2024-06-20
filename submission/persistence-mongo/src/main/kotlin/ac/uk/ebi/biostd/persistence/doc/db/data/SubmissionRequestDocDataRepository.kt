package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.model.action
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_CONFLICTED_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_DEPRECATED_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_IDX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_PREV_SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS_CHANGES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS_CHANGE_ENDTIME
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
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATUS
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
import ac.uk.ebi.biostd.persistence.doc.model.DocRequestStatusChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import com.google.common.collect.ImmutableList
import com.mongodb.BasicDBObject
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSING
import ebi.ac.uk.model.RequestStatus.PROCESSED
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
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
    suspend fun saveRequest(request: DocSubmissionRequest): Pair<DocSubmissionRequest, Boolean> {
        val result =
            mongoTemplate.upsert(
                Query(where(RQT_ACC_NO).`is`(request.accNo).andOperator(where(RQT_STATUS).ne(PROCESSED))),
                request.asSetOnInsert(),
                DocSubmissionRequest::class.java,
            ).awaitSingle()
        val created = result.matchedCount < 1
        return submissionRequestRepository.getByAccNoAndStatusIn(request.accNo, PROCESSING) to created
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
    ): DocSubmissionRequest {
        return submissionRequestRepository.getByAccNoAndVersion(accNo, version)
    }

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
        val update = Update().addToSet(RQT_STATUS_CHANGES, statusChange)
        val query = Query(where(RQT_ACC_NO).`is`(accNo).and(RQT_VERSION).`is`(version).and(RQT_STATUS).`is`(status))
        val result =
            mongoTemplate.findAndModify(
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
            mongoTemplate.find(query.skip(skip).limit(limit), DocSubmissionRequest::class.java)
                .asFlow()
                .toList()
        return result.count() to result
    }

    @Suppress("SpreadOperator")
    private fun createQuery(filter: SubmissionListFilter): Criteria =
        where("$SUB.$SUB_OWNER").`is`(filter.filterUser)
            .andOperator(*criteriaArray(filter))

    suspend fun increaseIndex(
        accNo: String,
        version: Int,
    ) {
        val update = Update().inc(RQT_IDX, 1).currentDate(RQT_MODIFICATION_TIME)
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
            where(RQT_FILE_SUB_ACC_NO).`is`(file.accNo)
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
            where(RQT_FILE_SUB_ACC_NO).`is`(file.accNo)
                .andOperator(
                    where(RQT_FILE_SUB_VERSION).`is`(file.version),
                    where(RQT_FILE_PATH).`is`(file.path),
                    where(RQT_PREVIOUS_SUB_FILE).`is`(file.previousSubFile),
                )
        mongoTemplate.updateFirst(Query(where), update, DocSubmissionRequestFile::class.java).awaitSingleOrNull()
    }

    suspend fun updateSubmissionRequest(rqt: DocSubmissionRequest) {
        val query = Query(where(SUB_ACC_NO).`is`(rqt.accNo).andOperator(where(SUB_VERSION).`is`(rqt.version)))
        val update =
            Update()
                .set(SUB_STATUS, rqt.status)
                .set(SUB, rqt.submission)
                .set(RQT_TOTAL_FILES, rqt.totalFiles)
                .set(RQT_IDX, rqt.currentIndex)
                .set(RQT_TOTAL_FILES, rqt.totalFiles)
                .set(RQT_MODIFICATION_TIME, rqt.modificationTime)
                .set(RQT_STATUS_CHANGES, rqt.statusChanges)

        mongoTemplate.updateFirst(query, update, DocSubmissionRequest::class.java).awaitSingleOrNull()
    }

    suspend fun updateSubmissionRequest(
        rqt: DocSubmissionRequest,
        processId: String,
        processEndTime: Instant,
        processRusult: ProcessResult,
    ) {
        val query =
            Query(
                where(SUB_ACC_NO).`is`(rqt.accNo).and(SUB_VERSION).`is`(rqt.version)
                    .and("$RQT_STATUS_CHANGES.$RQT_STATUS_CHANGE_STATUS_ID").`is`(ObjectId(processId)),
            )
        val update =
            Update()
                .set(SUB_STATUS, rqt.status)
                .set(SUB, rqt.submission)
                .set(RQT_TOTAL_FILES, rqt.totalFiles)
                .set(RQT_IDX, rqt.currentIndex)
                .set(RQT_TOTAL_FILES, rqt.totalFiles)
                .set(RQT_DEPRECATED_FILES, rqt.deprecatedFiles)
                .set(RQT_CONFLICTED_FILES, rqt.conflictingFiles)
                .set(RQT_MODIFICATION_TIME, rqt.modificationTime)
                .set(RQT_PREV_SUB_VERSION, rqt.previousVersion)
                .set("$RQT_STATUS_CHANGES.$.$RQT_STATUS_CHANGE_ENDTIME", processEndTime)
                .set("$RQT_STATUS_CHANGES.$.$RQT_STATUS_CHANGE_RESULT", processRusult.toString())
        mongoTemplate.updateFirst(query, update, DocSubmissionRequest::class.java).awaitSingleOrNull()
    }

    private fun criteriaArray(filter: SubmissionListFilter): Array<Criteria> =
        ImmutableList.Builder<Criteria>().apply {
            add(where(SUB_STATUS).`in`(PROCESSING))
            filter.accNo?.let { add(where("$SUB.$SUB_ACC_NO").`is`(it)) }
            filter.type?.let { add(where("$SUB.$SUB_SECTION.$SEC_TYPE").`is`(it)) }
            filter.rTimeFrom?.let { add(where("$SUB.$SUB_RELEASE_TIME").gte(it.toString())) }
            filter.rTimeTo?.let { add(where("$SUB.$SUB_RELEASE_TIME").lte(it.toString())) }
            filter.keywords?.let { add(keywordsCriteria(it)) }
            filter.released?.let { add(where("$SUB.$SUB_RELEASED").`is`(it)) }
        }.build().toTypedArray()

    private fun keywordsCriteria(keywords: String) =
        Criteria().orOperator(
            where("$SUB.$SUB_TITLE").regex("(?i).*$keywords.*"),
            where("$SUB.$SUB_SECTION.$SEC_ATTRIBUTES").elemMatch(
                where(ATTRIBUTE_DOC_NAME).`is`("Title").and(ATTRIBUTE_DOC_VALUE).regex("(?i).*$keywords.*"),
            ),
        )
}

enum class ProcessResult {
    SUCCESS,
    ERROR,
}
