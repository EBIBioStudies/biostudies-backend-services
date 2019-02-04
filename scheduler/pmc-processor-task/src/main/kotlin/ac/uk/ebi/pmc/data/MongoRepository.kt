package ac.uk.ebi.pmc.data

import ac.uk.ebi.pmc.data.docs.ErrorDoc
import ac.uk.ebi.pmc.data.docs.FileDoc
import ac.uk.ebi.pmc.data.docs.SubmissionDoc
import com.mongodb.async.client.FindIterable
import com.mongodb.async.client.MongoClient
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.findOne
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.coroutine.updateOne
import java.io.File

private const val SUBMISSION_COLLECTION = "submissions"
private const val ERRORS_COLLECTION = "errors"
private const val FILES_COLLECTION = "files"

class MongoRepository(
    private val dataBase: String,
    private val mongoClient: MongoClient
) {
    fun findSubmissions(sourceFile: String, imported: Boolean = false): FindIterable<SubmissionDoc> =
            getSubmissionCollection().find(and(eq("sourceFile", sourceFile), eq("imported", imported)))

    suspend fun save(submissionDoc: SubmissionDoc) = getSubmissionCollection().insertOne(submissionDoc)

    suspend fun save(errorDoc: ErrorDoc) = getCollection<ErrorDoc>(ERRORS_COLLECTION).insertOne(errorDoc)

    suspend fun update(submissionDoc: SubmissionDoc) = getSubmissionCollection().updateOne(submissionDoc)

    suspend fun saveSubFile(file: File, accNo: String): ObjectId {
        val collection = getFilesCollection()

        collection.insertOne(FileDoc(file.name, file.absolutePath, accNo))
        return collection.findOne(eq("path", file.absolutePath))!!.id
    }

    suspend fun getSubFiles(ids: List<ObjectId>): List<FileDoc> {
        val collection = getFilesCollection()

        return ids.map { collection.findOne(eq("_id", it))!! }.toList()
    }

    private fun getFilesCollection() = getCollection<FileDoc>(FILES_COLLECTION)

    private fun getSubmissionCollection() = getCollection<SubmissionDoc>(SUBMISSION_COLLECTION)

    private inline fun <reified T> getCollection(name: String) =
            mongoClient.getDatabase(dataBase).getCollection(name, T::class.java)
}
