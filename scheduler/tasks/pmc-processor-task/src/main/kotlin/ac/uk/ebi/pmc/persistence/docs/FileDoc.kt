package ac.uk.ebi.pmc.persistence.docs

import org.bson.types.ObjectId

@Suppress("ConstructorParameterNaming")
data class FileDoc(val name: String, val path: String, val accNo: String, val _id: ObjectId = ObjectId()) {

    companion object Fields {
        const val FILE_DOC_PATH = "path"
        const val FILE_DOC_ACC_NO = "accNo"
    }
}
