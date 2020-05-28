package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class OnBehalfRequest(
    private val userEmail: String,
    private val userName: String?,
    private val register: Boolean?
) {
    fun asRegisterRequest(): GetOrRegisterUserRequest {
        return GetOrRegisterUserRequest(register.orFalse(), userEmail, userName)
    }
}

sealed class SubmitWebRequest(
    val submitter: SecurityUser,
    val onBehalfRequest: OnBehalfRequest?,
    val format: SubFormat,
    val fileMode: FileMode,
    val files: List<File>,
    val attrs: Map<String, String>
)

class ContentSubmitWebRequest(
    val submission: String,
    onBehalfRequest: OnBehalfRequest?,
    user: SecurityUser,
    format: SubFormat,
    fileMode: FileMode,
    attrs: Map<String, String> = emptyMap(),
    files: List<File> = emptyList()
) : SubmitWebRequest(user, onBehalfRequest, format, fileMode, files, attrs)

class FileSubmitWebRequest(
    val submission: File,
    onBehalfRequest: OnBehalfRequest?,
    user: SecurityUser,
    format: SubFormat,
    fileMode: FileMode,
    attrs: Map<String, String> = emptyMap(),
    files: List<File> = emptyList()
) : SubmitWebRequest(user, onBehalfRequest, format, fileMode, files, attrs)

class RefreshWebRequest(
    val accNo: String,
    val user: SecurityUser
)
