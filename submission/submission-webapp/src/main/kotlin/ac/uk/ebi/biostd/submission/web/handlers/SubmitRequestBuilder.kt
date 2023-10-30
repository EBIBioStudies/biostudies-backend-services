package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ac.uk.ebi.biostd.submission.web.model.SubmissionConfig
import ac.uk.ebi.biostd.submission.web.model.SubmissionFilesConfig
import ac.uk.ebi.biostd.submission.web.model.SubmissionRequestParameters
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile

class SubmitRequestBuilder(
    private val tempFileGenerator: TempFileGenerator,
    private val onBehalfUtils: OnBehalfUtils,
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
            filesConfig = submitConfig.second
        )
    }

    fun buildFileRequest(
        submission: MultipartFile,
        request: SubmitBuilderRequest,
    ): FileSubmitWebRequest {
        val subFile = tempFileGenerator.asFile(submission)
        val submitConfig = submitConfig(request)
        return FileSubmitWebRequest(subFile, submitConfig.first, submitConfig.second)
    }

    private fun submitConfig(request: SubmitBuilderRequest): Pair<SubmissionConfig, SubmissionFilesConfig> {
        val (preferredSource, attributes, storageMode) = request.submissionRequestParameters
        val submissionConfig = SubmissionConfig(
            submitter = request.user,
            onBehalfUser = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
            attrs = attributes,
            storageMode = storageMode
        )
        val filesConfig = SubmissionFilesConfig(
            files = request.files?.let { tempFileGenerator.asFiles(it) },
            preferredSources = preferredSource
        )
        return submissionConfig to filesConfig
    }
}

data class SubmitBuilderRequest(
    val user: SecurityUser,
    val onBehalfRequest: OnBehalfRequest?,
    val submissionRequestParameters: SubmissionRequestParameters,
    val draftKey: String? = null,
    val files: List<MultipartFile>? = null,
)
