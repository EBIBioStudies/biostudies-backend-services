package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission
import ac.uk.ebi.biostd.submission.domain.helpers.RequestSources
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod.FILE
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class SubmissionWebHandler(
    private val submissionService: SubmissionService,
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService
) {
    fun submit(user: SecurityUser, files: List<File>, content: String, format: SubFormat, mode: FileMode): Submission {
        val sub = serializationService.deserializeSubmission(content, format)
        val source = sources(user, sub, files)
        val request = SubmissionRequest(submission(content, format, source), user, source, PAGE_TAB, mode)
        return submissionService.submit(request)
    }

    fun submit(user: SecurityUser, content: String, format: SubFormat, mode: FileMode): Submission {
        val sub = serializationService.deserializeSubmission(content, format)
        val source = sources(user, sub)
        val request = SubmissionRequest(submission(content, format, source), user, source, PAGE_TAB, mode)
        return submissionService.submit(request)
    }

    fun submit(user: SecurityUser, subFile: File, files: List<File>, attrs: Map<String, String>, mode: FileMode):
        Submission {
        val sub = serializationService.deserializeSubmission(subFile)
        val source = sources(user, sub, files.plus(subFile))
        val submission = subWithAttributes(subFile, attrs, source)
        return submissionService.submit(SubmissionRequest(submission, user, source, FILE, mode))
    }

    fun deleteSubmission(accNo: String, user: SecurityUser): Unit =
        submissionService.deleteSubmission(accNo, user)

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter): List<SimpleSubmission> =
        submissionService.getSubmissions(user, filter)

    private fun subWithAttributes(subFile: File, attrs: Map<String, String>, source: FilesSource): Submission {
        val submission = serializationService.deserializeSubmission(subFile, source)
        attrs.forEach { submission[it.key] = it.value }
        return submission
    }

    private fun sources(user: SecurityUser, submission: Submission, files: List<File> = emptyList()): FilesSource {
        return sourceGenerator.submissionSources(RequestSources(
            user = user,
            files = files,
            rootPath = submission.rootPath,
            subFolder = subFolder(submission.accNo)))
    }

    private fun submission(content: String, format: SubFormat, source: FilesSource) =
        serializationService.deserializeSubmission(content, format, source)

    private fun subFolder(accNo: String) = submissionService.submissionFolder(accNo)
}
