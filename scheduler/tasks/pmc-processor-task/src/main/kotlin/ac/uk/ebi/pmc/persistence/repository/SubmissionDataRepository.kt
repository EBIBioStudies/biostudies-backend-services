package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_BODY
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_FILES
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_ID
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_POS_IN_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_SOURCE_TIME
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_STATUS
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_UPDATED
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ErrorsRepository : CoroutineCrudRepository<SubmissionErrorDocument, ObjectId>

interface SubmissionDocRepository : CoroutineCrudRepository<SubmissionDocument, ObjectId> {
    suspend fun getById(objectId: ObjectId): SubmissionDocument
}

class SubmissionDataRepository(
    private val repository: SubmissionDocRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    suspend fun saveNew(sub: SubmissionDocument): Boolean {
        val inserted = tryToInsert(sub)
        if (inserted) expireOlderVersions(sub)
        return inserted
    }

    suspend fun findAll(): Flow<SubmissionDocument> = repository.findAll()

    suspend fun update(sub: SubmissionDocument): SubmissionDocument = repository.save(sub)

    suspend fun update(submissions: List<SubmissionDocument>) = repository.saveAll(submissions).collect()

    suspend fun findAndUpdate(
        status: SubmissionStatus,
        newStatus: SubmissionStatus,
    ): SubmissionDocument? {
        val query = Query(Criteria.where(SUB_STATUS).`is`(status))
        val update = Update().set(SUB_STATUS, newStatus)
        val options = FindAndModifyOptions().returnNew(true)
        return mongoTemplate.findAndModify(query, update, options, SubmissionDocument::class.java).awaitFirstOrNull()
    }

    suspend fun findAndUpdate(
        status: SubmissionStatus,
        newStatus: SubmissionStatus,
        sourceFile: String,
    ): SubmissionDocument? {
        val query =
            Query(
                Criteria
                    .where(SUB_STATUS)
                    .`is`(status)
                    .and(SUB_SOURCE_FILE)
                    .`is`(sourceFile),
            )
        val update = Update().set(SUB_STATUS, newStatus)
        val options = FindAndModifyOptions().returnNew(true)
        return mongoTemplate.findAndModify(query, update, options, SubmissionDocument::class.java).awaitFirstOrNull()
    }

    private suspend fun tryToInsert(sub: SubmissionDocument): Boolean {
        val newerVersion =
            where(SUB_ACC_NO).`is`(sub.accNo).andOperator(
                Criteria().orOperator(
                    where(SUB_SOURCE_TIME).gte(sub.sourceTime),
                    where(SUB_SOURCE_TIME).`is`(sub.sourceTime).and(SUB_POS_IN_FILE).gt(sub.posInFile),
                ),
            )
        var update =
            Update()
                .setOnInsert(SUB_ACC_NO, sub.accNo)
                .setOnInsert(SUB_ID, sub.id)
                .setOnInsert(SUB_BODY, sub.body)
                .setOnInsert(SUB_SOURCE_FILE, sub.sourceFile)
                .setOnInsert(SUB_POS_IN_FILE, sub.posInFile)
                .setOnInsert(SUB_SOURCE_TIME, sub.sourceTime)
                .setOnInsert(SUB_FILES, sub.files)
                .setOnInsert(SUB_STATUS, sub.status)
                .setOnInsert(SUB_UPDATED, sub.updated)

        val options = FindAndModifyOptions().returnNew(true).upsert(true)

        val result =
            mongoTemplate
                .findAndModify(
                    Query(newerVersion),
                    update,
                    options,
                    SubmissionDocument::class.java,
                ).awaitSingle()
        return result.id.equals(sub.id)
    }

    private suspend fun expireOlderVersions(sub: SubmissionDocument) {
        val olderVersions =
            where(SUB_ACC_NO).`is`(sub.accNo).andOperator(
                Criteria().orOperator(
                    where(SUB_SOURCE_TIME).lt(sub.sourceTime),
                    where(SUB_SOURCE_TIME).`is`(sub.sourceTime).and(SUB_POS_IN_FILE).lt(sub.posInFile),
                ),
            )
        val update = Update().set(SUB_STATUS, SubmissionStatus.DISCARDED)
        mongoTemplate
            .updateMulti(
                Query(olderVersions),
                update,
                SubmissionDocument::class.java,
            ).awaitFirst()
    }

    suspend fun getById(submissionId: ObjectId): SubmissionDocument = repository.getById(submissionId)
}
