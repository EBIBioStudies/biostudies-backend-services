package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class OnBehalfRequest(
    private val userEmail: String,
    private val userName: String?,
    private val register: Boolean?,
) {
    fun asRegisterRequest(): GetOrRegisterUserRequest =
        GetOrRegisterUserRequest(register.orFalse(), userEmail, userName)
}

data class SubmissionRequestParameters(
    val preferredSources: List<PreferredSource> = emptyList(),
    val attributes: List<ExtAttributeDetail> = emptyList(),
    val storageMode: StorageMode?,
)

data class SubmissionConfig(
    val submitter: SecurityUser,
    val onBehalfUser: SecurityUser?,
    val attrs: List<ExtAttributeDetail>,
    val storageMode: StorageMode?,
)

data class SubmissionFilesConfig(
    val files: List<File>?,
    val preferredSources: List<PreferredSource>,
)

sealed class SubmitWebRequest(
    val config: SubmissionConfig,
    val filesConfig: SubmissionFilesConfig,
)

val SubmitWebRequest.method: SubmissionMethod
    get() = when (this) {
        is ContentSubmitWebRequest -> SubmissionMethod.PAGE_TAB
        is FileSubmitWebRequest -> SubmissionMethod.FILE
    }

val SubmitWebRequest.draftKey: String?
    get() = when (this) {
        is ContentSubmitWebRequest -> draftKey
        is FileSubmitWebRequest -> null
    }

class ContentSubmitWebRequest(
    val submission: String,
    val draftKey: String? = null,
    val format: SubFormat,
    submissionConfig: SubmissionConfig,
    filesConfig: SubmissionFilesConfig,
) : SubmitWebRequest(submissionConfig, filesConfig)

class FileSubmitWebRequest(
    val submission: File,
    submissionConfig: SubmissionConfig,
    filesConfig: SubmissionFilesConfig,
) : SubmitWebRequest(submissionConfig, filesConfig)
