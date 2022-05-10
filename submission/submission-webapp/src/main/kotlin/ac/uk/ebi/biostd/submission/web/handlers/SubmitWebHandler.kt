package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.exceptions.ConcurrentProcessingSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSubmission
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
    private val extSubmissionService: ExtSubmissionService,
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
        val sub = serializationService.deserializeSubmission(request.submission, request.format)
        val extSub = extSubmissionService.findExtendedSubmission(sub.accNo)?.also { requireProcessed(it) }

        val source = sourceGenerator.submissionSources(
            RequestSources(
                submitter = request.submitter,
                files = request.files,
                owner = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
                rootPath = sub.rootPath,
                submission = extSub
            )
        )
        val submission = withAttributes(submission(request.submission, request.format, source), request.attrs)

        return SubmitRequest(
            submission = submission,
            submitter = request.submitter,
            onBehalfUser = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
            method = PAGE_TAB,
            sources = source,
            mode = request.fileMode,
            draftKey = request.draftKey
        )
    }

    private fun buildRequest(request: FileSubmitWebRequest): SubmitRequest {
        val sub = serializationService.deserializeSubmission(request.submission)
        val extSub = extSubmissionService.findExtendedSubmission(sub.accNo)?.apply { requireProcessed(this) }

        val source = sourceGenerator.submissionSources(
            RequestSources(
                submitter = request.submitter,
                files = request.files,
                owner = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
                rootPath = sub.rootPath,
                submission = extSub
            )
        )
        val submission = withAttributes(submission(request.submission, source), request.attrs)

        userFilesService.uploadFile(request.submitter, DIRECT_UPLOAD_PATH, request.submission)

        return SubmitRequest(
            submission = submission,
            submitter = request.submitter,
            onBehalfUser = request.onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) },
            sources = source,
            method = FILE,
            mode = request.fileMode
        )
    }

    private fun withAttributes(submission: Submission, attrs: Map<String, String?>): Submission {
        attrs.forEach { submission[it.key] = it.value }
        return submission
    }

    private fun submission(content: String, format: SubFormat, source: FilesSource) =
        serializationService.deserializeSubmission(content, format, source)

    private fun submission(subFile: File, source: FilesSource) =
        serializationService.deserializeSubmission(subFile, source)

    private fun requireProcessed(sub: ExtSubmission) =
        require(sub.status == PROCESSED) { throw ConcurrentProcessingSubmissionException(sub.accNo) }
}
