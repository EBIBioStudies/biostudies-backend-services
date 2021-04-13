package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.ext.findOneAndUpdate
import com.mongodb.async.client.MongoCollection
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
import org.litote.kmongo.SetTo
import org.litote.kmongo.coroutine.drop
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.coroutine.updateMany
import org.litote.kmongo.coroutine.updateOne
import java.time.Instant

class SubmissionRepository(private val submissions: MongoCollection<SubmissionDoc>) {

    suspend fun insertOrExpire(submission: SubmissionDoc) {
        val latest = getLatest(submission)
        submissions.updateMany(
            expireSubmissions(latest.accno, latest.sourceTime, latest.posInFile),
            SetTo(SubmissionDoc::status, SubmissionStatus.DISCARDED)
        )
    }

    suspend fun insert(submission: SubmissionDoc) {
        submissions.insertOne(submission)
    }

    suspend fun findAll() = submissions.find().toList()

    private suspend fun getLatest(submission: SubmissionDoc) =
        submissions.findOneAndUpdate(
            latest(submission.accno, submission.sourceTime, submission.posInFile),
            submission.asInsertOrExpire(),
            FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        )!!

    suspend fun update(submissionDoc: SubmissionDoc) = submissions.updateOne(submissionDoc)

    suspend fun findAndUpdate(status: SubmissionStatus, newStatus: SubmissionStatus) = submissions.findOneAndUpdate(
        eq(SubmissionDoc.status, status.name),
        combine(set(SubmissionDoc.status, newStatus.name), set(SubmissionDoc.updated, Instant.now())),
        FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
    )

    private fun latest(accNo: String, sourceTime: Instant, posInFile: Int) =
        and(eq(SubmissionDoc.accNo, accNo), or(
            gte(SubmissionDoc.sourceTime, sourceTime), and(
            eq(SubmissionDoc.sourceTime, sourceTime), gt(SubmissionDoc.posInFile, posInFile)))
        )

    private fun expireSubmissions(accNo: String, sourceTime: Instant, posInFile: Int) =
        and(eq(SubmissionDoc.accNo, accNo), or(
            lt(SubmissionDoc.sourceTime, sourceTime), and(
            eq(SubmissionDoc.sourceTime, sourceTime), lt(SubmissionDoc.posInFile, posInFile)))
        )

    suspend fun deleteAll() = submissions.drop()
}
