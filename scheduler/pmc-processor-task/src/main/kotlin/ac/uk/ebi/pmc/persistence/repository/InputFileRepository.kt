package ac.uk.ebi.pmc.persistence.repository

import ac.uk.ebi.pmc.load.FileSpec
import ac.uk.ebi.pmc.persistence.docs.InputFileDoc
import arrow.core.toOption
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.Filters
import org.litote.kmongo.coroutine.findOne
import org.litote.kmongo.coroutine.insertOne

class InputFileRepository(private val collection: MongoCollection<InputFileDoc>) {

    suspend fun save(fileDoc: FileSpec) = collection.insertOne(InputFileDoc(fileDoc.name))

    suspend fun find(file: FileSpec) = collection.findOne(Filters.eq(InputFileDoc.name, file.name)).toOption()
}
