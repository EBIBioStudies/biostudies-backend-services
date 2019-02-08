package ac.uk.ebi.pmc.data.repository

import ac.uk.ebi.pmc.data.docs.SubStatus
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import arrow.core.toOption
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.coroutine.findOneAndUpdate
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.coroutine.updateOne
import java.time.Instant

class SubRepository(private val submissions: MongoCollection<SubmissionDoc>) {

    suspend fun save(errorDoc: SubmissionDoc) = submissions.insertOne(errorDoc)

    suspend fun find(sourceFile: String, imported: Boolean = false) =
        submissions.find(and(eq(SubmissionDoc.sourceFile, sourceFile), eq(SubmissionDoc.imported, imported))).toList()

    suspend fun update(submissionDoc: SubmissionDoc) = submissions.updateOne(submissionDoc)

    suspend fun findNext(status: SubStatus, newStatus: SubStatus) = submissions.findOneAndUpdate(
        eq(SubmissionDoc.status, status.name).toString(),
        "{$set: {${SubmissionDoc.status}: '${newStatus.name}', ${SubmissionDoc.updated} : ${Instant.now()}}}")
        .toOption()
}
