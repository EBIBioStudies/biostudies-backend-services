package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import com.mongodb.async.client.MongoCollection
import org.litote.kmongo.coroutine.insertOne

class ErrorsRepository(private val collection: MongoCollection<SubmissionErrorDoc>) {

    suspend fun save(errorDoc: SubmissionErrorDoc) = collection.insertOne(errorDoc)
}
