package uk.ac.ebi.biostd.io.sources

import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFile
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
