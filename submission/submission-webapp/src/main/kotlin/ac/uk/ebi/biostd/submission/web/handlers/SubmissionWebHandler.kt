package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ac.uk.ebi.biostd.submission.model.UserSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile

class SubmissionWebHandler(
    private val submissionService: SubmissionService,
    private val tempFileGenerator: TempFileGenerator,
    private val serializationService: SerializationService
) {
    fun submit(user: SecurityUser, files: Array<MultipartFile>, content: String, format: SubFormat):
        Submission {
        val fileSource = UserSource(tempFileGenerator.asFiles(files), user.magicFolder.path)
        val submission = serializationService.deserializeSubmission(content, format, fileSource)
        return submissionService.submit(submission, user, fileSource)
    }

    fun submit(user: SecurityUser, content: String, format: SubFormat): Submission {
        val fileSource = UserSource(emptyList(), user.magicFolder.path)
        val submission = serializationService.deserializeSubmission(content, format, fileSource)
        return submissionService.submit(submission, user, fileSource)
    }

    fun submit(user: SecurityUser, multipartFile: MultipartFile, files: Array<MultipartFile>): Submission {
        val file = tempFileGenerator.asFile(multipartFile)
        val fileSource = UserSource(tempFileGenerator.asFiles(files), user.magicFolder.path)
        val format = submissionService.getFormat(file)
        val submission = serializationService.deserializeSubmission(file.readText(), format)
        return submissionService.submit(submission, user, fileSource)
    }

    fun deleteSubmission(accNo: String, user: SecurityUser): Unit = submissionService.deleteSubmission(accNo, user)
}
