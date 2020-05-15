package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ac.uk.ebi.biostd.submission.web.model.RefreshWebRequest
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod.FILE
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

typealias Request = SubmissionRequest

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

class SubmitWebHandler(
    private val submissionService: SubmissionService,
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService,
    private val userFilesService: UserFilesService,
    private val securityService: ISecurityService
) {
    fun submit(request: ContentSubmitWebRequest): Submission {
        val sub = serializationService.deserializeSubmission(request.submission, request.format)
        val source = sources(request.submitter, sub, request.files)
        val submission = withAttributes(submission(request.submission, request.format, source), request.attrs)
        return submissionService.submit(Request(
            submission = submission,
            submitter = request.submitter,
            onBehalfUser = request.onBehalfRequest?.let { getOnBehalfUser(it) },
            method = PAGE_TAB,
            sources = source,
            mode = request.fileMode
        ))
    }

    fun submit(request: FileSubmitWebRequest): Submission {
        val sub = serializationService.deserializeSubmission(request.submission)
        val source = sources(request.submitter, sub, request.files.plus(request.submission))
        val submission = withAttributes(submission(request.submission, source), request.attrs)
        userFilesService.uploadFile(request.submitter, DIRECT_UPLOAD_PATH, request.submission)
        return submissionService.submit(Request(
            submission = submission,
            submitter = request.submitter,
            onBehalfUser = request.onBehalfRequest?.let { getOnBehalfUser(it) },
            sources = source,
            method = FILE,
            mode = request.fileMode
        ))
    }

    fun refreshSubmission(request: RefreshWebRequest): Submission {
        val submission = submissionService.getSubmission(request.accNo).toSimpleSubmission()
        val source = sources(request.user, submission, emptyList())
        return submissionService.submit(Request(
            submission = submission,
            submitter = request.user,
            sources = source,
            method = PAGE_TAB,
            mode = FileMode.MOVE
        ))
    }

    private fun getOnBehalfUser(request: OnBehalfRequest): SecurityUser =
        securityService.getOrRegisterUser(request.asRegisterRequest())

    private fun sources(user: SecurityUser, submission: Submission, files: List<File>): FilesSource {
        return sourceGenerator.submissionSources(RequestSources(
            user = user,
            files = files,
            rootPath = submission.rootPath,
            subFolder = subFolder(submission.accNo)
        ))
    }

    private fun withAttributes(submission: Submission, attrs: Map<String, String>): Submission {
        attrs.forEach { submission[it.key] = it.value }
        return submission
    }

    private fun submission(content: String, format: SubFormat, source: FilesSource) =
        serializationService.deserializeSubmission(content, format, source)

    private fun submission(subFile: File, source: FilesSource) =
        serializationService.deserializeSubmission(subFile, source)

    private fun subFolder(accNo: String) = submissionService.submissionFolder(accNo)
}
