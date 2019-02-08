package ac.uk.ebi.pmc.data.repository

import ac.uk.ebi.pmc.data.docs.FileDoc
import ac.uk.ebi.pmc.data.ext.getOne
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.coroutine.toList
import java.io.File

class SubFileRepository(private val collection: MongoCollection<FileDoc>) {

    suspend fun getFiles(ids: List<ObjectId>) = collection.find(Filters.`in`("_id", ids)).toList()

    suspend fun saveFile(file: File, accNo: String): ObjectId {
        collection.insertOne(FileDoc(file.name, file.absolutePath, accNo))
        return collection.getOne(Filters.eq("path", file.absolutePath)).id
    }
}
