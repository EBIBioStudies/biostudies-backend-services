package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.ext.findOneAndUpdate
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.gt
import com.mongodb.client.model.Filters.lt
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import org.litote.kmongo.SetTo
import org.litote.kmongo.coroutine.updateMany
import org.litote.kmongo.coroutine.updateOne
import java.time.Instant

class SubmissionRepository(private val submissions: MongoCollection<SubmissionDoc>) {

    suspend fun insertIfLastOne(submission: SubmissionDoc, sourceTime: Instant) {
        submissions.findOneAndUpdate(
            and(eq(SubmissionDoc.accNo, submission.accno), gt(SubmissionDoc.sourceTime, sourceTime)),
            submission.asInsertOnUpdate(),
            FindOneAndUpdateOptions().upsert(true))
    }

    suspend fun update(submissionDoc: SubmissionDoc) = submissions.updateOne(submissionDoc)

    suspend fun findAndUpdate(status: SubmissionStatus, newStatus: SubmissionStatus) = submissions.findOneAndUpdate(
        eq(SubmissionDoc.status, status.name),
        combine(set(SubmissionDoc.status, newStatus.name), set(SubmissionDoc.updated, Instant.now())))

    suspend fun expireSubmissions(accNo: String, sourceTime: Instant) =
        submissions.updateMany(
            and(eq(SubmissionDoc.accNo, accNo), lt(SubmissionDoc.sourceTime, sourceTime)),
            SetTo(SubmissionDoc::status, SubmissionStatus.DISCARDED))
}
