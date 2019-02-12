package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import arrow.core.toOption
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.lt
import com.mongodb.client.model.Updates
import org.litote.kmongo.coroutine.findOneAndUpdate
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.coroutine.updateMany
import org.litote.kmongo.coroutine.updateOne
import java.time.Instant

class SubRepository(private val submissions: MongoCollection<SubmissionDoc>) {

    suspend fun save(errorDoc: SubmissionDoc) = submissions.insertOne(errorDoc)

    suspend fun find(sourceFile: String, imported: Boolean = false) =
        submissions.find(and(eq(SubmissionDoc.sourceFile, sourceFile), eq(SubmissionDoc.imported, imported))).toList()

    suspend fun update(submissionDoc: SubmissionDoc) = submissions.updateOne(submissionDoc)

    suspend fun findNext(status: SubmissionStatus, newStatus: SubmissionStatus) = submissions.findOneAndUpdate(
        eq(SubmissionDoc.status, status.name).toString(),
        "{set: {${SubmissionDoc.status}: '${newStatus.name}', ${SubmissionDoc.updated} : ${Instant.now()}}}")
        .toOption()

    suspend fun expireOldVersions(accNo: String, sourceTime: Instant) =
        submissions.updateMany(
            and(eq(SubmissionDoc.accNo, accNo), lt(SubmissionDoc.sourceTime, sourceTime)).toString(),
            Updates.set(SubmissionDoc.status, SubmissionStatus.DISCARDED).toString())
}
