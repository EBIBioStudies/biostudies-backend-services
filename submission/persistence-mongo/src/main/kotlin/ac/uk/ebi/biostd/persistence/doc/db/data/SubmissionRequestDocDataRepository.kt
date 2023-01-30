package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.Companion.PROCESSING
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_IDX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_TOTAL_FILES
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
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import com.google.common.collect.ImmutableList
import com.mongodb.BasicDBObject
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Update.update
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.Instant

@Suppress("TooManyFunctions")
class SubmissionRequestDocDataRepository(
    private val mongoTemplate: MongoTemplate,
    private val extSerializationService: ExtSerializationService,
    private val submissionRequestRepository: SubmissionRequestRepository,
) : SubmissionRequestRepository by submissionRequestRepository {
    fun saveRequest(request: DocSubmissionRequest): Pair<DocSubmissionRequest, Boolean> {
        val result = mongoTemplate.upsert(
            Query(where(RQT_ACC_NO).`is`(request.accNo).andOperator(where(RQT_STATUS).ne(PROCESSED))),
            request.asSetOnInsert(),
            DocSubmissionRequest::class.java
        )
        return submissionRequestRepository.save(request) to (result.matchedCount < 1)
    }

    fun findActiveRequests(filter: SubmissionFilter, email: String? = null): Pair<Int, List<DocSubmissionRequest>> {
        val query = Query().addCriteria(createQuery(filter, email))
        val requestCount = mongoTemplate.count(query, DocSubmissionRequest::class.java)
        return when {
            requestCount <= filter.offset -> requestCount.toInt() to emptyList()
            else -> findActiveRequests(query, filter.offset, filter.limit)
        }
    }

    private fun findActiveRequests(
        query: Query,
        skip: Long,
        limit: Int,
    ): Pair<Int, MutableList<DocSubmissionRequest>> {
        val result = mongoTemplate.find(query.skip(skip).limit(limit), DocSubmissionRequest::class.java)
        return result.count() to result
    }

    @Suppress("SpreadOperator")
    private fun createQuery(filter: SubmissionFilter, email: String? = null): Criteria =
        where("$SUB.$SUB_OWNER").`is`(email)
            .andOperator(*criteriaArray(filter))

    fun updateIndex(accNo: String, version: Int, index: Int) {
        val update = Update().set(RQT_IDX, index).set(RQT_MODIFICATION_TIME, Instant.now())
        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).`is`(version)))

        mongoTemplate.updateFirst(query, update, DocSubmissionRequest::class.java)
    }

    fun upsertSubmissionRequestFile(rqtFile: SubmissionRequestFile) {
        val file = BasicDBObject.parse(extSerializationService.serialize(rqtFile.file))
        val update = update(RQT_FILE_FILE, file).set(RQT_FILE_INDEX, rqtFile.index)
        val where = where(RQT_FILE_SUB_ACC_NO).`is`(rqtFile.accNo)
            .andOperator(where(RQT_FILE_SUB_VERSION).`is`(rqtFile.version), where(RQT_FILE_PATH).`is`(rqtFile.path))

        mongoTemplate.upsert(Query(where), update, DocSubmissionRequestFile::class.java)
    }

    fun updateSubmissionRequestFile(rqtFile: SubmissionRequestFile) {
        val file = BasicDBObject.parse(extSerializationService.serialize(rqtFile.file))
        val update = update(RQT_FILE_FILE, file).set(RQT_FILE_INDEX, rqtFile.index)
        val where = where(RQT_FILE_SUB_ACC_NO).`is`(rqtFile.accNo)
            .andOperator(where(RQT_FILE_SUB_VERSION).`is`(rqtFile.version), where(RQT_FILE_PATH).`is`(rqtFile.path))
        mongoTemplate.updateFirst(Query(where), update, DocSubmissionRequestFile::class.java)
    }

    fun updateSubmissionRequest(rqt: DocSubmissionRequest) {
        val query = Query(where(SUB_ACC_NO).`is`(rqt.accNo).andOperator(where(SUB_VERSION).`is`(rqt.version)))
        val update = Update()
            .set(SUB_STATUS, rqt.status)
            .set(SUB, rqt.submission)
            .set(RQT_TOTAL_FILES, rqt.totalFiles)
            .set(RQT_IDX, rqt.currentIndex)
            .set(RQT_TOTAL_FILES, rqt.totalFiles)
            .set(RQT_MODIFICATION_TIME, rqt.modificationTime)

        mongoTemplate.updateFirst(query, update, DocSubmissionRequest::class.java)
    }

    private fun criteriaArray(filter: SubmissionFilter): Array<Criteria> =
        ImmutableList.Builder<Criteria>().apply {
            add(where(SUB_STATUS).`in`(PROCESSING))
            filter.accNo?.let { add(where("$SUB.$SUB_ACC_NO").`is`(it)) }
            filter.type?.let { add(where("$SUB.$SUB_SECTION.$SEC_TYPE").`is`(it)) }
            filter.rTimeFrom?.let { add(where("$SUB.$SUB_RELEASE_TIME").gte(it.toString())) }
            filter.rTimeTo?.let { add(where("$SUB.$SUB_RELEASE_TIME").lte(it.toString())) }
            filter.keywords?.let { add(keywordsCriteria(it)) }
            filter.released?.let { add(where("$SUB.$SUB_RELEASED").`is`(it)) }
        }.build().toTypedArray()

    private fun keywordsCriteria(keywords: String) = Criteria().orOperator(
        where("$SUB.$SUB_TITLE").regex("(?i).*$keywords.*"),
        where("$SUB.$SUB_SECTION.$SEC_ATTRIBUTES").elemMatch(
            where(ATTRIBUTE_DOC_NAME).`is`("Title").and(ATTRIBUTE_DOC_VALUE).regex("(?i).*$keywords.*")
        )
    )
}
