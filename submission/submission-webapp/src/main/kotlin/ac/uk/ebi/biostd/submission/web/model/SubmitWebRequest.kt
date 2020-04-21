package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

sealed class SubmitWebRequest(
    val user: SecurityUser,
    val format: SubFormat,
    val fileMode: FileMode,
    val files: List<File>,
    val attrs: Map<String, String>
)

class ContentSubmitWebRequest(
    val submission: String,
    user: SecurityUser,
    format: SubFormat,
    fileMode: FileMode,
    attrs: Map<String, String> = emptyMap(),
    files: List<File> = emptyList()
) : SubmitWebRequest(user, format, fileMode, files, attrs)

class FileSubmitWebRequest(
    val submission: File,
    user: SecurityUser,
    format: SubFormat,
    fileMode: FileMode,
    attrs: Map<String, String> = emptyMap(),
    files: List<File> = emptyList()
) : SubmitWebRequest(user, format, fileMode, files, attrs)
