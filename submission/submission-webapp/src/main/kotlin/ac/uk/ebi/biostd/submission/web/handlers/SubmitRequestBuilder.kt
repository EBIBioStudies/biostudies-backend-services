package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.helpers.TempFileGenerator
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
            onBehalfRequest = request.onBehalfRequest,
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
        return FileSubmitWebRequest(
            submission = subFile,
            onBehalfRequest = request.onBehalfRequest,
            submissionConfig = submitConfig.first,
            filesConfig = submitConfig.second
        )
    }

    private fun submitConfig(request: SubmitBuilderRequest): Pair<SubmissionConfig, SubmissionFilesConfig> {
        val tempFiles = request.files?.let { tempFileGenerator.asFiles(it) }
        val (preferredSource, attributes) = request.submissionRequestParameters

        val submissionConfig = SubmissionConfig(request.user, attributes)
        val filesConfig = SubmissionFilesConfig(tempFiles, preferredSource)
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
