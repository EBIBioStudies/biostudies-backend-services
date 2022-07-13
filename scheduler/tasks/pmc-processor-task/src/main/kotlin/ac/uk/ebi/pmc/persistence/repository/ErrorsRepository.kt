package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.litote.kmongo.coroutine.toList

class ErrorsRepository(private val collection: MongoCollection<SubmissionErrorDoc>) {

    suspend fun save(errorDoc: SubmissionErrorDoc): InsertOneResult = collection.insertOne(errorDoc).awaitSingle()

    suspend fun findAll(): List<SubmissionErrorDoc> = collection.find().toList()

    suspend fun deleteAll(): Any? = collection.drop().awaitFirstOrNull()
}
