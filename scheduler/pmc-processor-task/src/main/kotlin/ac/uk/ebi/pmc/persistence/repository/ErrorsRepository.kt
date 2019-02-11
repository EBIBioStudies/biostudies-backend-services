package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.ErrorDoc
import com.mongodb.async.client.MongoCollection
import org.litote.kmongo.coroutine.insertOne

class ErrorsRepository(private val collection: MongoCollection<ErrorDoc>) {

    suspend fun save(errorDoc: ErrorDoc) = collection.insertOne(errorDoc)
}
