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
    fun getNotImportedSubmissionsBySourceFile(sourceFile: String): FindIterable<SubmissionDoc> =
            getSubmissionCollection().find(and(eq("sourceFile", sourceFile), eq("imported", false)))

    suspend fun save(submissionDoc: SubmissionDoc) = getSubmissionCollection().insertOne(submissionDoc)

    suspend fun save(errorDoc: ErrorDoc) = getCollection(ERRORS_COLLECTION, ErrorDoc::class.java).insertOne(errorDoc)

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

    private fun getFilesCollection() = getCollection(FILES_COLLECTION, FileDoc::class.java)

    private fun getSubmissionCollection() = getCollection(SUBMISSION_COLLECTION, SubmissionDoc::class.java)

    private fun <A, B : Class<A>> getCollection(name: String, documentClass: B) =
            mongoClient.getDatabase(dataBase).getCollection(name, documentClass)
}
