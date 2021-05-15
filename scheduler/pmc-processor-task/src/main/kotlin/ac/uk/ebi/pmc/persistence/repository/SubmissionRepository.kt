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
import org.litote.kmongo.reactivestreams.updateOne
import org.litote.kmongo.setValue
import java.time.Instant

class SubmissionRepository(private val submissions: MongoCollection<SubmissionDoc>) {

    suspend fun insertOrExpire(submission: SubmissionDoc) {
        val latest = getLatest(submission)
        submissions.updateMany(
            expireSubmissions(latest.accno, latest.sourceTime, latest.posInFile),
            setValue(SubmissionDoc::status, SubmissionStatus.DISCARDED)
        ).awaitSingle()
    }

    suspend fun findAll() = submissions.find().asFlow().toList()

    private suspend fun getLatest(submission: SubmissionDoc): SubmissionDoc =
        submissions.findOneAndUpdate(
            latest(submission.accno, submission.sourceTime, submission.posInFile),
            submission.asInsertOrExpire(),
            FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        ).awaitFirst()

    suspend fun update(submissionDoc: SubmissionDoc): UpdateResult = submissions.updateOne(submissionDoc).awaitFirst()

    suspend fun findAndUpdate(status: SubmissionStatus, newStatus: SubmissionStatus): SubmissionDoc? =
        submissions.findOneAndUpdate(
            eq(SubmissionDoc.status, status.name),
            combine(set(SubmissionDoc.status, newStatus.name), set(SubmissionDoc.updated, Instant.now())),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitFirstOrNull()

    private fun latest(accNo: String, sourceTime: Instant, posInFile: Int) =
        and(
            eq(SubmissionDoc.accNo, accNo),
            or(
                gte(SubmissionDoc.sourceTime, sourceTime),
                and(eq(SubmissionDoc.sourceTime, sourceTime), gt(SubmissionDoc.posInFile, posInFile))
            )
        )

    private fun expireSubmissions(accNo: String, sourceTime: Instant, posInFile: Int) =
        and(
            eq(SubmissionDoc.accNo, accNo),
            or(
                lt(SubmissionDoc.sourceTime, sourceTime),
                and(eq(SubmissionDoc.sourceTime, sourceTime), lt(SubmissionDoc.posInFile, posInFile))
            )
        )

    suspend fun deleteAll() {
        submissions.drop().awaitFirstOrNull()
    }
}
