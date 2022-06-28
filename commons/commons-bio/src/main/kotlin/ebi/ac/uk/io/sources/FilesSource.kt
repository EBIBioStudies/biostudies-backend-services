package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

interface FilesSource {
    fun getExtFile(path: String, md5: String? = null, attributes: List<Attribute> = emptyList()): ExtFile?
    fun getFile(path: String, md5: String? = null): File?
    fun description(): String
}
