package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class OnBehalfRequest(
    private val userEmail: String,
    private val userName: String?,
    private val register: Boolean?
) {
    fun asRegisterRequest(): GetOrRegisterUserRequest =
        GetOrRegisterUserRequest(register.orFalse(), userEmail, userName)
}

data class SubmissionRequestParameters(
    val preferredSources: List<PreferredSource> = emptyList(),
    val attributes: List<ExtAttributeDetail> = emptyList(),
)

data class SubmissionConfig(
    val format: SubFormat,
    val submitter: SecurityUser,
    val attrs: List<ExtAttributeDetail>,
)

data class SubmissionFilesConfig(
    val files: List<File>,
    val preferredSources: List<PreferredSource>,
)

sealed class SubmitWebRequest(
    val submissionConfig: SubmissionConfig,
    val onBehalfRequest: OnBehalfRequest?,
    val filesConfig: SubmissionFilesConfig,
)

class ContentSubmitWebRequest(
    val submission: String,
    val draftKey: String? = null,
    submissionConfig: SubmissionConfig,
    onBehalfRequest: OnBehalfRequest?,
    filesConfig: SubmissionFilesConfig,
) : SubmitWebRequest(submissionConfig, onBehalfRequest, filesConfig)

class FileSubmitWebRequest(
    val submission: File,
    submissionConfig: SubmissionConfig,
    onBehalfRequest: OnBehalfRequest?,
    filesConfig: SubmissionFilesConfig,
) : SubmitWebRequest(submissionConfig, onBehalfRequest, filesConfig)
