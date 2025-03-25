package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import java.io.File

enum class PreferredSource { SUBMISSION, USER_SPACE }

interface FilesSource {
    val description: String

    suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile?

    suspend fun getFileList(path: String): File?
}
