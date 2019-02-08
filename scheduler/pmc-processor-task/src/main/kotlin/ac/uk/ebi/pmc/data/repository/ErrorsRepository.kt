package ac.uk.ebi.pmc.data.repository

import ac.uk.ebi.pmc.data.docs.ErrorDoc
import com.mongodb.async.client.MongoCollection
import org.litote.kmongo.coroutine.insertOne

class ErrorsRepository(private val collection: MongoCollection<ErrorDoc>) {

    suspend fun save(errorDoc: ErrorDoc) = collection.insertOne(errorDoc)
}
