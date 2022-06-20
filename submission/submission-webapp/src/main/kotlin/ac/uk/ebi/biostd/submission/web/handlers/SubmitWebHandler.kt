package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.exceptions.ConcurrentProcessingSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod.FILE
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import ebi.ac.uk.model.extensions.rootPath
import java.io.File

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

@Suppress("TooManyFunctions", "LongParameterList")
class SubmitWebHandler(
    private val submissionService: SubmissionService,
    private val extSubmissionService: ExtSubmissionQueryService,
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService,
    private val userFilesService: UserFilesService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val onBehalfUtils: OnBehalfUtils
) {
    fun submit(request: ContentSubmitWebRequest): Submission =
        toSubmissionMapper.toSimpleSubmission(submissionService.submit(buildRequest(request)))

    fun submit(request: FileSubmitWebRequest): Submission =
        toSubmissionMapper.toSimpleSubmission(submissionService.submit(buildRequest(request)))

    fun submitAsync(request: ContentSubmitWebRequest) = submissionService.submitAsync(buildRequest(request))

    fun submitAsync(request: FileSubmitWebRequest) = submissionService.submitAsync(buildRequest(request))

    private fun buildRequest(request: ContentSubmitWebRequest): SubmitRequest {
        val (format, submitter, attrs) = request.submissionConfig
        val (fileMode, files, preferredSource) = request.filesConfig
        val sub = serializationService.deserializeSubmission(request.submission, format)
        val extSub = extSubmissionService.findExtendedSubmission(sub.accNo)?.also { requireNotProcessing(it.accNo) }

        val source = sourceGenerator.submissionSources(
            RequestSources(
                submitter = submitter,
                files = files,
                owner = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
                rootPath = sub.rootPath,
                submission = extSub,
                preferredSources = preferredSource
            )
        )
        val submission = withAttributes(submission(request.submission, format, source), attrs)

        return SubmitRequest(
            submission = submission,
            submitter = submitter,
            onBehalfUser = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
            method = PAGE_TAB,
            sources = source,
            mode = fileMode,
            draftKey = request.draftKey
        )
    }

    private fun buildRequest(request: FileSubmitWebRequest): SubmitRequest {
        val (_, submitter, attrs) = request.submissionConfig
        val (fileMode, files, preferredSource) = request.filesConfig
        val sub = serializationService.deserializeSubmission(request.submission)
        val extSub = extSubmissionService.findExtendedSubmission(sub.accNo)
        requireNotProcessing(sub.accNo)

        val source = sourceGenerator.submissionSources(
            RequestSources(
                submitter = submitter,
                files = files,
                owner = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
                rootPath = sub.rootPath,
                submission = extSub,
                preferredSources = preferredSource
            )
        )
        val submission = withAttributes(submission(request.submission, source), attrs)

        userFilesService.uploadFile(submitter, DIRECT_UPLOAD_PATH, request.submission)

        return SubmitRequest(
            submission = submission,
            submitter = submitter,
            onBehalfUser = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
            sources = source,
            method = FILE,
            mode = fileMode
        )
    }

    private fun withAttributes(submission: Submission, attrs: List<ExtAttributeDetail>): Submission {
        attrs.forEach { submission[it.name] = it.value }
        return submission
    }

    private fun submission(content: String, format: SubFormat, source: FilesSource) =
        serializationService.deserializeSubmission(content, format, source)

    private fun submission(subFile: File, source: FilesSource) =
        serializationService.deserializeSubmission(subFile, source)

    private fun requireNotProcessing(accNo: String) = require(extSubmissionService.hasPendingRequest(accNo).not()) {
        throw ConcurrentProcessingSubmissionException(accNo)
    }
}
