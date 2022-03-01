package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.FileMode
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

@Suppress("LongParameterList")
sealed class SubmitWebRequest(
    val submitter: SecurityUser,
    val onBehalfRequest: OnBehalfRequest?,
    val format: SubFormat,
    val fileMode: FileMode,
    val files: List<File>,
    val attrs: Map<String, String?>
)

@Suppress("LongParameterList")
class ContentSubmitWebRequest(
    val submission: String,
    val draftKey: String? = null,
    onBehalfRequest: OnBehalfRequest?,
    user: SecurityUser,
    format: SubFormat,
    fileMode: FileMode,
    attrs: Map<String, String?> = emptyMap(),
    files: List<File> = emptyList()
) : SubmitWebRequest(user, onBehalfRequest, format, fileMode, files, attrs)

@Suppress("LongParameterList")
class FileSubmitWebRequest(
    val submission: File,
    onBehalfRequest: OnBehalfRequest?,
    user: SecurityUser,
    format: SubFormat,
    fileMode: FileMode,
    attrs: Map<String, String?> = emptyMap(),
    files: List<File> = emptyList()
) : SubmitWebRequest(user, onBehalfRequest, format, fileMode, files, attrs)
