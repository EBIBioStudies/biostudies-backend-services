package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.exceptions.ConcurrentProcessingSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allSectionsFiles
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod.FILE
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

@Suppress("TooManyFunctions")
class SubmitWebHandler(
    private val submissionService: SubmissionService,
    private val extSubmissionService: ExtSubmissionService,
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService,
    private val userFilesService: UserFilesService,
    private val webHelper: WebHandlerHelper
) {
    fun submit(request: ContentSubmitWebRequest): Submission =
        submissionService.submit(buildRequest(request)).toSimpleSubmission()

    fun submit(request: FileSubmitWebRequest): Submission =
        submissionService.submit(buildRequest(request)).toSimpleSubmission()

    fun submitAsync(request: ContentSubmitWebRequest) = submissionService.submitAsync(buildRequest(request))

    fun submitAsync(request: FileSubmitWebRequest) = submissionService.submitAsync(buildRequest(request))

    private fun buildRequest(request: ContentSubmitWebRequest): SubmitRequest {
        val sub = serializationService.deserializeSubmission(request.submission, request.format)
        val extSub = extSubmissionService.findExtendedSubmission(sub.accNo)?.also { webHelper.requireProcessed(it) }

        val source = sourceGenerator.submissionSources(
            RequestSources(
                user = request.submitter,
                files = request.files,
                rootPath = sub.rootPath,
                previousFiles = extSub?.allSectionsFiles.orEmpty()
            )
        )
        val submission = withAttributes(submission(request.submission, request.format, source), request.attrs)

        return SubmitRequest(
            submission = submission,
            submitter = request.submitter,
            onBehalfUser = request.onBehalfRequest?.let { webHelper.getOnBehalfUser(it) },
            method = PAGE_TAB,
            sources = source,
            mode = request.fileMode,
            draftKey = request.draftKey
        )
    }

    private fun buildRequest(request: FileSubmitWebRequest): SubmitRequest {
        val sub = serializationService.deserializeSubmission(request.submission)
        val extSub = extSubmissionService.findExtendedSubmission(sub.accNo)?.apply { webHelper.requireProcessed(this) }

        val source = sourceGenerator.submissionSources(
            RequestSources(
                user = request.submitter,
                files = request.files.plus(request.submission),
                rootPath = sub.rootPath,
                previousFiles = extSub?.let { it.allSectionsFiles }.orEmpty()
            )
        )
        val submission = withAttributes(submission(request.submission, source), request.attrs)
        userFilesService.uploadFile(request.submitter, DIRECT_UPLOAD_PATH, request.submission)
        return SubmitRequest(
            submission = submission,
            submitter = request.submitter,
            onBehalfUser = request.onBehalfRequest?.let { webHelper.getOnBehalfUser(it) },
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

    private fun submission(
        subFile: File,
        source: FilesSource
    ) = serializationService.deserializeSubmission(subFile, source)
}
