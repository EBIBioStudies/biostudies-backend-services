package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FileOrigin.EMPTY_SOURCE
import ebi.ac.uk.model.Attribute
import java.io.File

enum class FileOrigin { EMPTY_SOURCE, MIXED, FIRE, GROUP_SPACE, SUBMISSION, USER_SPACE }

interface FilesSource {
    val filesOrigin: FileOrigin

    fun getExtFile(path: String, md5: String? = null, attributes: List<Attribute> = emptyList()): ExtFile?

    fun getFile(path: String, md5: String? = null): File?

    companion object {
        val EMPTY_FILE_SOURCE: FilesSource = object : FilesSource {
            override val filesOrigin: FileOrigin
                get() = EMPTY_SOURCE

            override fun getExtFile(path: String, md5: String?, attributes: List<Attribute>): ExtFile? = null

            override fun getFile(path: String, md5: String?): File? = null
        }
    }
}
