package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.gt
import com.mongodb.client.model.Filters.gte
import com.mongodb.client.model.Filters.lt
import com.mongodb.client.model.Filters.or
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.ObjectId
import org.litote.kmongo.reactivestreams.updateOne
import org.litote.kmongo.setValue
import java.time.Instant

class SubmissionRepository(private val submissions: MongoCollection<SubmissionDoc>) {

    suspend fun insertOrExpire(submission: SubmissionDoc) {
        val latest = getLatest(submission)
        submissions.updateMany(
            expireSubmissions(latest.accNo, latest.sourceTime, latest.posInFile),
            setValue(SubmissionDoc::status, SubmissionStatus.DISCARDED)
        ).awaitSingle()
    }

    suspend fun findAll() = submissions.find().asFlow().toList()

    private suspend fun getLatest(submission: SubmissionDoc): SubmissionDoc =
        submissions.findOneAndUpdate(
            latest(submission.accNo, submission.sourceTime, submission.posInFile),
            submission.asInsertOrExpire(),
            FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        ).awaitFirst()

    suspend fun update(submissionDoc: SubmissionDoc): UpdateResult = submissions.updateOne(submissionDoc).awaitFirst()

    suspend fun findByIdAndUpdate(id: String, newStatus: SubmissionStatus): SubmissionDoc? =
        submissions.findOneAndUpdate(
            eq(SubmissionDoc.SUB_ID, ObjectId(id)),
            combine(set(SubmissionDoc.SUB_STATUS, newStatus.name), set(SubmissionDoc.SUB_UPDATED, Instant.now())),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitFirstOrNull()

    suspend fun findAndUpdate(status: SubmissionStatus, newStatus: SubmissionStatus): SubmissionDoc? =
        submissions.findOneAndUpdate(
            eq(SubmissionDoc.SUB_STATUS, status.name),
            combine(set(SubmissionDoc.SUB_STATUS, newStatus.name), set(SubmissionDoc.SUB_UPDATED, Instant.now())),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitFirstOrNull()

    private fun latest(accNo: String, sourceTime: Instant, posInFile: Int) =
        and(
            eq(SubmissionDoc.SUB_ACC_NO, accNo),
            or(
                gte(SubmissionDoc.SUB_SOURCE_TIME, sourceTime),
                and(eq(SubmissionDoc.SUB_SOURCE_TIME, sourceTime), gt(SubmissionDoc.SUB_POS_IN_FILE, posInFile))
            )
        )

    private fun expireSubmissions(accNo: String, sourceTime: Instant, posInFile: Int) =
        and(
            eq(SubmissionDoc.SUB_ACC_NO, accNo),
            or(
                lt(SubmissionDoc.SUB_SOURCE_TIME, sourceTime),
                and(eq(SubmissionDoc.SUB_SOURCE_TIME, sourceTime), lt(SubmissionDoc.SUB_POS_IN_FILE, posInFile))
            )
        )

    suspend fun deleteAll() {
        submissions.drop().awaitFirstOrNull()
    }
}
