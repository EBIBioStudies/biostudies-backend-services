package ac.uk.ebi.biostd.persistence.service.filesystem

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.extended.model.allReferencedFiles
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.deleteFile
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.FileUtils.reCreateFolder
import ebi.ac.uk.io.ONLY_USER
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

private val READ_ONLY_GROUP = PosixFilePermissions.fromString("rwxr-x---")

class FilesService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {

    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        val submissionPath = folderResolver.getSubmissionFolder(submission.relPath)
        generateOutputFiles(submission, submissionPath)
        when (mode) {
            FileMode.MOVE -> processFiles(submission, submissionPath, ::move)
            FileMode.COPY -> processFiles(submission, submissionPath, ::copy)
        }
    }

    private fun generateOutputFiles(submission: ExtSubmission, submissionPath: Path) {
        val simpleSubmission = submission.toSimpleSubmission()

        generateOutputFiles(simpleSubmission, submissionPath, submission.accNo)
        submission.allFileList.forEach { generateOutputFiles(it.toFilesTable(), submissionPath, it.fileName) }
    }

    private fun <T> generateOutputFiles(element: T, submissionPath: Path, fileName: String) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)

        FileUtils.writeContent(submissionPath.resolve("$fileName.json").toFile(), json, READ_ONLY_GROUP)
        FileUtils.writeContent(submissionPath.resolve("$fileName.xml").toFile(), xml, READ_ONLY_GROUP)
        FileUtils.writeContent(submissionPath.resolve("$fileName.pagetab.tsv").toFile(), tsv, READ_ONLY_GROUP)
    }

    private fun processFiles(submission: ExtSubmission, submissionPath: Path, process: (ExtFile, File) -> Unit) {
        val submissionFolder = getSubmissionFolder(submissionPath)
        val temporally = createTempFolder(submissionFolder, submission.accNo)

        submission.allFiles.forEach { process(it, temporally) }
        submission.allReferencedFiles.forEach { process(it, temporally) }

        val filesPath = submissionFolder.resolve(FILES_PATH)
        deleteFile(filesPath)
        moveFile(temporally, filesPath)
    }

    private fun copy(extFile: ExtFile, file: File) =
        copyOrReplaceFile(extFile.file, file.resolve(extFile.fileName), READ_ONLY_GROUP)

    private fun move(file: ExtFile, path: File) =
        moveFile(file.file, path.resolve(file.fileName), READ_ONLY_GROUP)

    private fun getSubmissionFolder(submissionPath: Path): File =
        getOrCreateFolder(submissionPath, READ_ONLY_GROUP).toFile()

    private fun createTempFolder(submissionFolder: File, accNo: String): File =
        reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), ONLY_USER)
}
