package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.persistence.docs.FileDoc
import ac.uk.ebi.pmc.persistence.ext.getOne
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.Success
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.toList
import java.io.File

class SubFileRepository(private val collection: MongoCollection<FileDoc>) {

    suspend fun getFiles(ids: List<ObjectId>): List<FileDoc> = collection.find(Filters.`in`("_id", ids)).toList()

    suspend fun saveFile(file: File, accNo: String): ObjectId {
        collection.insertOne(FileDoc(file.name, file.absolutePath, accNo)).awaitSingle()
        return collection.getOne(Filters.eq("path", file.absolutePath)).id
    }

    suspend fun findAll(): List<FileDoc> = collection.find().toList()

    suspend fun deleteAll(): Success = collection.drop().awaitSingle()
}
