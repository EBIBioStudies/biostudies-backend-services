package ac.uk.ebi.pmc.persistence.docs

import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Suppress("VariableNaming")
data class FileDoc(val name: String, val path: String, val accNo: String) {

    val _id: Id<FileDoc> = newId()

    val id: ObjectId
        get() = ObjectId(_id.toString())

    companion object Fields {
        const val FILE_DOC_NAME = "name"
        const val FILE_DOC_PATH = "path"
        const val FILE_DOC_ACC_NO = "accNo"
    }
}
