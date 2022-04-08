package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute

interface FilesSource {
    fun getFile(path: String, md5: String? = null, attributes: List<Attribute> = emptyList()): ExtFile?

    companion object {
        val EMPTY_FILE_SOURCE: FilesSource = object : FilesSource {
            override fun getFile(
                path: String,
                md5: String?,
                attributes: List<Attribute>
            ): ExtFile? = null
        }
    }
}
