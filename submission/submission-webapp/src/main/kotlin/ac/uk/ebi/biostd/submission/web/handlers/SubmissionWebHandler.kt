package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class SubmissionWebHandler(
    private val submissionService: SubmissionService,
    private val sourceGenerator: SourceGenerator,
    private val serializationService: SerializationService
) {
    fun submit(user: SecurityUser, files: List<File>, content: String, format: SubFormat): Submission {
        val filesSource = sourceGenerator.getSubmissionSources(user, files, rootPath(content, format))
        val submission = serializationService.deserializeSubmission(content, format, filesSource)
        return submissionService.submit(SubmissionRequest(submission, user, filesSource, SubmissionMethod.PAGE_TAB))
    }

    fun submit(user: SecurityUser, content: String, format: SubFormat): Submission {
        val fileSource = sourceGenerator.getSubmissionSources(user, rootPath(content, format))
        val submission = serializationService.deserializeSubmission(content, format, fileSource)
        return submissionService.submit(SubmissionRequest(submission, user, fileSource, SubmissionMethod.PAGE_TAB))
    }

    fun submit(user: SecurityUser, subFile: File, files: List<File>, subAttrs: Map<String, String>): Submission {
        val fileSource = sourceGenerator.getSubmissionSources(user, files.plus(subFile), rootPath(subFile))
        val submission = serializationService.deserializeSubmission(subFile, fileSource)
        subAttrs.forEach { submission[it.key] = it.value }

        return submissionService.submit(SubmissionRequest(submission, user, fileSource, SubmissionMethod.FILE))
    }

    fun submit(user: SecurityUser, subFile: File, files: List<File>, attachTo: String?): Submission {
        val fileSource = sourceGenerator.getSubmissionSources(user, files.plus(subFile), rootPath(subFile))
        val submission = serializationService.deserializeSubmission(subFile, fileSource)
        attachTo?.let { submission.attachTo = attachTo }
        return submissionService.submit(SubmissionRequest(submission, user, fileSource, SubmissionMethod.FILE))
    }

    fun deleteSubmission(accNo: String, user: SecurityUser): Unit = submissionService.deleteSubmission(accNo, user)

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter): List<SimpleSubmission> =
        submissionService.getSubmissions(user, filter)

    private fun rootPath(subFile: File) =
        serializationService.deserializeSubmission(subFile).rootPath.orEmpty()

    private fun rootPath(content: String, format: SubFormat) =
        serializationService.deserializeSubmission(content, format).rootPath.orEmpty()
}
