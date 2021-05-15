package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.load.FileSpec
import ac.uk.ebi.pmc.persistence.docs.InputFileDoc
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.Success
import kotlinx.coroutines.reactive.awaitFirst
import org.litote.kmongo.coroutine.toList

class InputFileRepository(private val collection: MongoCollection<InputFileDoc>) {

    suspend fun save(fileDoc: FileSpec): Success = collection.insertOne(InputFileDoc(fileDoc.name)).awaitFirst()

    suspend fun findAll(): List<InputFileDoc> = collection.find().toList()
}
