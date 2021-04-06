package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import com.mongodb.async.client.MongoCollection
import org.litote.kmongo.coroutine.drop
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.coroutine.toList

class ErrorsRepository(private val collection: MongoCollection<SubmissionErrorDoc>) {

    suspend fun save(errorDoc: SubmissionErrorDoc) = collection.insertOne(errorDoc)

    suspend fun findAll() = collection.find().toList()

    suspend fun deleteAll() = collection.drop()
}
