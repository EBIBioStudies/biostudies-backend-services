package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactive.awaitSingle
import org.litote.kmongo.coroutine.toList

class ErrorsRepository(private val collection: MongoCollection<SubmissionErrorDoc>) {

    suspend fun save(errorDoc: SubmissionErrorDoc) = collection.insertOne(errorDoc).awaitSingle()

    suspend fun findAll() = collection.find().toList()

    suspend fun deleteAll() = collection.drop().awaitSingle()
}
