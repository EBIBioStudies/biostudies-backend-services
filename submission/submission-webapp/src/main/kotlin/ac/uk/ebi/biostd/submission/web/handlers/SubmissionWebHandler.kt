package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.util.SubmissionFilter
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ac.uk.ebi.biostd.submission.model.GroupFileSource
import ebi.ac.uk.io.isExcel
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.ListFilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
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
        val filesSource = ComposedFileSource(
            PathFilesSource(user.magicFolder.path.resolve(rootPath(content, format))),
            ListFilesSource(tempFileGenerator.asFiles(files)),
            *getGroupSources(user.groupsFolders))
        val submission = serializationService.deserializeSubmission(content, format, filesSource)
        return submissionService.submit(submission, user, filesSource)
    }

    fun submit(user: SecurityUser, content: String, format: SubFormat): Submission {
        val fileSource = ComposedFileSource(
            PathFilesSource(user.magicFolder.path.resolve(rootPath(content, format))),
            *getGroupSources(user.groupsFolders))
        val submission = serializationService.deserializeSubmission(content, format, fileSource)
        return submissionService.submit(submission, user, fileSource)
    }

    fun submit(user: SecurityUser, multipartFile: MultipartFile, files: Array<MultipartFile>): Submission {
        val file = tempFileGenerator.asFile(multipartFile)
        val format = serializationService.getSubmissionFormat(file)
        val content = readSubmissionFile(file)

        val filesSource = ComposedFileSource(
            PathFilesSource(user.magicFolder.path.resolve(rootPath(content, format))),
            ListFilesSource(tempFileGenerator.asFiles(files)),
            *getGroupSources(user.groupsFolders))

        val submission = serializationService.deserializeSubmission(content, format, filesSource)
        return submissionService.submit(submission, user, filesSource)
    }

    fun deleteSubmission(accNo: String, user: SecurityUser): Unit = submissionService.deleteSubmission(accNo, user)

    fun getSubmissions(user: SecurityUser, filter: SubmissionFilter) = submissionService.getSubmissions(user, filter)

    private fun rootPath(submission: String, format: SubFormat) =
        serializationService.deserializeSubmission(submission, format).rootPath.orEmpty()

    private fun readSubmissionFile(file: File) =
        if (file.isExcel()) excelReader.readContentAsTsv(file) else file.readText()

    private fun getGroupSources(groups: List<GroupMagicFolder>): Array<FilesSource> =
        groups.map { GroupFileSource(it.groupName, it.path) }.toTypedArray()
}


