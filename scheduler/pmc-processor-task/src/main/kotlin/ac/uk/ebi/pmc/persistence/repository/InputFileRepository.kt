package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.load.FileSpec
import ac.uk.ebi.pmc.persistence.docs.InputFileDoc
import ac.uk.ebi.pmc.persistence.docs.InputFileStatus.FAILED
import ac.uk.ebi.pmc.persistence.docs.InputFileStatus.PROCESSED
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.litote.kmongo.coroutine.toList
import java.io.File

class InputFileRepository(private val collection: MongoCollection<InputFileDoc>) {

    suspend fun saveProcessed(fileDoc: FileSpec): InsertOneResult =
        collection.insertOne(InputFileDoc(name = fileDoc.name, status = PROCESSED)).awaitFirst()

    suspend fun saveFailed(file: File): InsertOneResult =
        collection.insertOne(InputFileDoc(name = file.name, status = FAILED)).awaitFirst()

    suspend fun findAll(): List<InputFileDoc> = collection.find().toList()

    suspend fun deleteAll() = collection.drop().awaitFirstOrNull()
}
