package ac.uk.ebi.pmc.importer.data.docs

import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class FileDoc(val name: String, val path: String, val accNo: String) {

    val _id: Id<FileDoc> = newId()

    val id: ObjectId
        get() = ObjectId(_id.toString())
}
