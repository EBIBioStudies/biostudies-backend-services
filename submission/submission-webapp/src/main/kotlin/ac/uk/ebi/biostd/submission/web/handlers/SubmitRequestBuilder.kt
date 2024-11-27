package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.model.SubmissionConfig
import ac.uk.ebi.biostd.submission.model.SubmissionFilesConfig
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class SubmitRequestBuilder(
    private val onBehalfUtils: OnBehalfUtils,
    private val submissionTaskProperties: SubmissionTaskProperties,
) {
    fun buildContentRequest(
        submission: String,
        format: SubFormat,
        request: SubmitBuilderRequest,
    ): ContentSubmitWebRequest {
        val submitConfig = submitConfig(request)
        return ContentSubmitWebRequest(
            submission = submission,
            format = format,
            draftKey = request.draftKey,
            submissionConfig = submitConfig.first,
            filesConfig = submitConfig.second,
        )
    }

    fun buildFileRequest(
        submission: File,
        request: SubmitBuilderRequest,
    ): FileSubmitWebRequest {
        val submitConfig = submitConfig(request)
        return FileSubmitWebRequest(submission, submitConfig.first, submitConfig.second)
    }

    private fun submitConfig(request: SubmitBuilderRequest): Pair<SubmissionConfig, SubmissionFilesConfig> {
        val (preferredSource, attributes, storageMode, silentMode, singleJobMode) = request.submissionRequestParameters
        val submissionConfig =
            SubmissionConfig(
                submitter = request.user,
                onBehalfUser = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
                attrs = attributes.map { ExtAttributeDetail(it.name, it.value) },
                storageMode = storageMode,
                silentMode = silentMode.orFalse(),
                singleJobMode = singleJobMode ?: submissionTaskProperties.singleJobMode,
            )
        val filesConfig =
            SubmissionFilesConfig(
                files = request.files,
                preferredSources = preferredSource,
            )
        return submissionConfig to filesConfig
    }
}

data class SubmitBuilderRequest(
    val user: SecurityUser,
    val onBehalfRequest: OnBehalfParameters?,
    val submissionRequestParameters: SubmitParameters,
    val draftKey: String? = null,
    val files: List<File>? = null,
)
