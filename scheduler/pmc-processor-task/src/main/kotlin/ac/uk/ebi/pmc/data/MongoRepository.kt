package ac.uk.ebi.pmc.data

import ac.uk.ebi.pmc.data.docs.ErrorDoc
import ac.uk.ebi.pmc.data.docs.FileDoc
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import com.mongodb.async.client.FindIterable
import com.mongodb.async.client.MongoClient
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.findOne
import org.litote.kmongo.coroutine.insertOne
import java.io.File

private const val SUBMISSION_COLLECTION = "submissions"
private const val ERRORS_COLLECTION = "errors"
private const val FILES_COLLECTION = "files"

class MongoRepository(
    private val dataBase: String,
    private val mongoClient: MongoClient
) {
    fun getAllSubmissions(): FindIterable<SubmissionDoc> {
        val database = mongoClient.getDatabase(dataBase)
        val collection = database.getCollection(SUBMISSION_COLLECTION, SubmissionDoc::class.java)

        return collection.find()
    }

    suspend fun save(submissionDoc: SubmissionDoc) {
        val database = mongoClient.getDatabase(dataBase)
        val collection = database.getCollection(SUBMISSION_COLLECTION, SubmissionDoc::class.java)
        collection.insertOne(submissionDoc)
    }

    suspend fun save(errorDoc: ErrorDoc) {
        val database = mongoClient.getDatabase(dataBase)
        val collection = database.getCollection(ERRORS_COLLECTION, ErrorDoc::class.java)
        collection.insertOne(errorDoc)
    }

    suspend fun saveSubFile(file: File, accNo: String): ObjectId {
        val database = mongoClient.getDatabase(dataBase)
        val collection = database.getCollection(FILES_COLLECTION, FileDoc::class.java)
        collection.insertOne(FileDoc(file.name, file.absolutePath, accNo))
        return collection.findOne(Filters.eq("path", file.absolutePath))!!.id
    }

    suspend fun getSubFiles(ids: List<ObjectId>): List<FileDoc> {
        val database = mongoClient.getDatabase(dataBase)
        val collection = database.getCollection(FILES_COLLECTION, FileDoc::class.java)

        return ids.map { collection.findOne(Filters.eq("_id", it))!! }.toList()
    }
}
