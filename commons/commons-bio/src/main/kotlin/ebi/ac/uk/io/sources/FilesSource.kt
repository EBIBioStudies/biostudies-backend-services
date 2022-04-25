package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileOrigin
import ebi.ac.uk.extended.model.ExtFileOrigin.USER_SPACE
import ebi.ac.uk.model.Attribute
import java.io.File

interface FilesSource {
    abstract val filesOrigin: ExtFileOrigin

    fun getExtFile(
        path: String,
        md5: String? = null,
        attributes: List<Attribute> = emptyList(),
        preferredOrigin: ExtFileOrigin = USER_SPACE
    ): ExtFile?

    fun getFile(path: String, md5: String? = null): File?

    companion object {
        val EMPTY_FILE_SOURCE: FilesSource = object : FilesSource {
            override val filesOrigin: ExtFileOrigin
                get() = USER_SPACE

            override fun getExtFile(
                path: String,
                md5: String?,
                attributes: List<Attribute>,
                preferredOrigin: ExtFileOrigin
            ): ExtFile? = null

            override fun getFile(path: String, md5: String?): File? = null
        }
    }
}
