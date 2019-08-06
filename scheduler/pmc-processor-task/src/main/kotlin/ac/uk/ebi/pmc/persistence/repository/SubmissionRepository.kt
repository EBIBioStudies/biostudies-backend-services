package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.ext.findOneAndUpdate
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.lt
import com.mongodb.client.model.Filters.or
import com.mongodb.client.model.Sorts.descending
import com.mongodb.client.model.Sorts.orderBy
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import org.litote.kmongo.SetTo
import org.litote.kmongo.coroutine.first
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.coroutine.updateMany
import org.litote.kmongo.coroutine.updateOne
import java.time.Instant

class SubmissionRepository(private val submissions: MongoCollection<SubmissionDoc>) {
    suspend fun insertOrExpire(submission: SubmissionDoc) {
        getLatest(submission)?.let { if (submission.isNewerOrEqual(it)) submission.status = SubmissionStatus.DISCARDED }
        submissions.insertOne(submission)

        val latest = getLatest(submission)!!

        submissions.updateMany(
            expireSubmissions(latest.accno, latest.sourceTime, latest.posInFile),
            SetTo(SubmissionDoc::status, SubmissionStatus.DISCARDED))
    }

    private suspend fun getLatest(submission: SubmissionDoc) =
        submissions
            .find(eq(SubmissionDoc.accNo, submission.accno))
            .sort(orderBy(descending(SubmissionDoc.sourceTime), descending(SubmissionDoc.posInFile))).first()

    suspend fun update(submissionDoc: SubmissionDoc) = submissions.updateOne(submissionDoc)

    suspend fun findAndUpdate(status: SubmissionStatus, newStatus: SubmissionStatus) = submissions.findOneAndUpdate(
        eq(SubmissionDoc.status, status.name),
        combine(set(SubmissionDoc.status, newStatus.name), set(SubmissionDoc.updated, Instant.now())))


    private fun expireSubmissions(accNo: String, sourceTime: Instant, posInFile: Int) =
        and(eq(SubmissionDoc.accNo, accNo), or(
            lt(SubmissionDoc.sourceTime, sourceTime),
            and(eq(SubmissionDoc.sourceTime, sourceTime), lt(SubmissionDoc.posInFile, posInFile))))
}
