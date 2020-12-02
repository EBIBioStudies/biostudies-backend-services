package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.exceptions.ConcurrentProcessingSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ac.uk.ebi.biostd.submission.web.model.RefreshWebRequest
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod.FILE
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

@Suppress("TooManyFunctions")
class SubmitWebHandler(
    private val submissionService: SubmissionService,
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService,
    private val userFilesService: UserFilesService,
    private val securityService: ISecurityService,
    private val folderResolver: SubmissionFolderResolver
) {
    fun submit(request: ContentSubmitWebRequest): Submission =
        submissionService.submit(buildRequest(request)).toSimpleSubmission()

    fun submit(request: FileSubmitWebRequest): Submission =
        submissionService.submit(buildRequest(request)).toSimpleSubmission()

    fun submitAsync(request: ContentSubmitWebRequest) = submissionService.submitAsync(buildRequest(request))

    fun submitAsync(request: FileSubmitWebRequest): Unit = submissionService.submitAsync(buildRequest(request))

    private fun buildRequest(request: ContentSubmitWebRequest): SubmissionRequest {
        val sub = serializationService.deserializeSubmission(request.submission, request.format)
        val previousVersion = submissionService.findPreviousVersion(sub.accNo)
        requireNotProcessing(previousVersion)

        val source = sourceGenerator.submissionSources(RequestSources(
            user = request.submitter,
            files = request.files,
            rootPath = sub.rootPath,
            subFolder = subFolder(previousVersion)
        ))
        val submission = withAttributes(submission(request.submission, request.format, source), request.attrs)
        return SubmissionRequest(
            submission = submission,
            submitter = request.submitter,
            onBehalfUser = request.onBehalfRequest?.let { getOnBehalfUser(it) },
            method = PAGE_TAB,
            sources = source,
            mode = request.fileMode
        )
    }

    private fun buildRequest(request: FileSubmitWebRequest): SubmissionRequest {
        val sub = serializationService.deserializeSubmission(request.submission)
        val previousVersion = submissionService.findPreviousVersion(sub.accNo)
        requireNotProcessing(previousVersion)

        val source = sourceGenerator.submissionSources(RequestSources(
            user = request.submitter,
            files = request.files.plus(request.submission),
            rootPath = sub.rootPath,
            subFolder = subFolder(previousVersion)
        ))
        val submission = withAttributes(submission(request.submission, source), request.attrs)
        userFilesService.uploadFile(request.submitter, DIRECT_UPLOAD_PATH, request.submission)
        return SubmissionRequest(
            submission = submission,
            submitter = request.submitter,
            onBehalfUser = request.onBehalfRequest?.let { getOnBehalfUser(it) },
            sources = source,
            method = FILE,
            mode = request.fileMode
        )
    }

    fun refreshSubmission(request: RefreshWebRequest): Submission {
        val submission = submissionService.getSubmission(request.accNo).toSimpleSubmission()
        val previousVersion = submissionService.findPreviousVersion(submission.accNo)
        requireNotProcessing(previousVersion)

        val source = sourceGenerator.submissionSources(RequestSources(subFolder = subFolder(previousVersion)))
        return submissionService.submit(SubmissionRequest(
            submission = submission,
            submitter = request.user,
            sources = source,
            method = PAGE_TAB,
            mode = FileMode.MOVE
        )).toSimpleSubmission()
    }

    private fun getOnBehalfUser(request: OnBehalfRequest): SecurityUser =
        securityService.getOrRegisterUser(request.asRegisterRequest())

    private fun withAttributes(submission: Submission, attrs: Map<String, String>): Submission {
        attrs.forEach { submission[it.key] = it.value }
        return submission
    }

    private fun submission(content: String, format: SubFormat, source: FilesSource) =
        serializationService.deserializeSubmission(content, format, source)

    private fun submission(subFile: File, source: FilesSource) =
        serializationService.deserializeSubmission(subFile, source)

    private fun subFolder(submission: BasicSubmission?): File? =
        submission?.let { folderResolver.getSubFolder(submission.relPath).toFile().resolve(FILES_PATH) }

    private fun requireNotProcessing(basicSubmission: BasicSubmission?) =
        basicSubmission?.let {
            if (it.isActive) require(it.status == PROCESSED) {
                throw ConcurrentProcessingSubmissionException(basicSubmission.accNo)
            }
        }
}
