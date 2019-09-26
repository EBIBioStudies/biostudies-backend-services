package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ebi.ac.uk.io.isExcel
import ebi.ac.uk.io.sources.ComposeFileSource
import ebi.ac.uk.io.sources.ListFilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.util.file.ExcelReader
import org.springframework.web.multipart.MultipartFile
import java.io.File

class SubmissionWebHandler(
    private val excelReader: ExcelReader,
    private val submissionService: SubmissionService,
    private val tempFileGenerator: TempFileGenerator,
    private val serializationService: SerializationService
) {
    fun submit(user: SecurityUser, files: Array<MultipartFile>, content: String, format: SubFormat):
        Submission {
        val filesSource = ComposeFileSource(
            PathFilesSource(user.magicFolder.path.resolve(getRootPath(content, format))),
            ListFilesSource(tempFileGenerator.asFiles(files)))
        val submission = serializationService.deserializeSubmission(content, format, filesSource)
        return submissionService.submit(submission, user, filesSource)
    }

    fun submit(user: SecurityUser, content: String, format: SubFormat): Submission {
        val fileSource = ComposeFileSource(PathFilesSource(user.magicFolder.path.resolve(getRootPath(content, format))))
        val submission = serializationService.deserializeSubmission(content, format, fileSource)
        return submissionService.submit(submission, user, fileSource)
    }

    fun submit(user: SecurityUser, multipartFile: MultipartFile, files: Array<MultipartFile>): Submission {
        val file = tempFileGenerator.asFile(multipartFile)
        val format = serializationService.getSubmissionFormat(file)
        val content = readSubmissionFile(file)

        val filesSource = ComposeFileSource(
            PathFilesSource(user.magicFolder.path.resolve(getRootPath(content, format))),
            ListFilesSource(tempFileGenerator.asFiles(files)))

        val submission = serializationService.deserializeSubmission(content, format, filesSource)
        return submissionService.submit(submission, user, filesSource)
    }

    fun deleteSubmission(accNo: String, user: SecurityUser): Unit = submissionService.deleteSubmission(accNo, user)

    private fun getRootPath(submission: String, format: SubFormat) =
        serializationService.deserializeSubmission(submission, format).rootPath.orEmpty()

    private fun readSubmissionFile(file: File) =
        if (file.isExcel()) excelReader.readContentAsTsv(file) else file.readText()
}
