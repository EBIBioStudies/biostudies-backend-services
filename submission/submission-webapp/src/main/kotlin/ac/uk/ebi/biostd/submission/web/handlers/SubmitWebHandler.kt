package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod.FILE
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

private const val DIRECT_UPLOAD_PATH = "direct-uploads"

class SubmitWebHandler(
    private val submissionService: SubmissionService,
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService,
    private val userFilesService: UserFilesService
) {
    fun submit(request: ContentSubmitWebRequest): Submission {
        val sub = serializationService.deserializeSubmission(request.submission, request.format)
        val source = sources(request.user, sub, request.files)
        val submission = withAttributes(submission(request.submission, request.format, source), request.attrs)
        return submissionService.submit(SubmissionRequest(submission, request.user, source, PAGE_TAB, request.fileMode))
    }

    fun submit(request: FileSubmitWebRequest): Submission {
        val sub = serializationService.deserializeSubmission(request.submission)
        val source = sources(request.user, sub, request.files.plus(request.submission))
        val submission = withAttributes(submission(request.submission, source), request.attrs)
        userFilesService.uploadFile(request.user, DIRECT_UPLOAD_PATH, request.submission)
        return submissionService.submit(SubmissionRequest(submission, request.user, source, FILE, request.fileMode))
    }

    private fun sources(user: SecurityUser, submission: Submission, files: List<File> = emptyList()): FilesSource {
        val request = RequestSources(user, files, submission.rootPath, subFolder(submission.accNo))
        return sourceGenerator.submissionSources(request)
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
