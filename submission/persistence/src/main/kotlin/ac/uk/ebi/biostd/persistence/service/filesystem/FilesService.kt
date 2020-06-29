package ac.uk.ebi.biostd.persistence.service.filesystem

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ac.uk.ebi.biostd.persistence.integration.FileMode.COPY
import ac.uk.ebi.biostd.persistence.integration.FileMode.MOVE
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
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

private val READ_ONLY_GROUP: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-x---")
private val ALL_READ: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-xr-x")

@Suppress("TooManyFunctions")
class FilesService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        val permissions = permissions(submission.released)
        val submissionPath = folderResolver.getSubmissionFolder(submission.relPath)

        generatePageTab(submission, submissionPath, permissions)
        processSubmissionAttachedFiles(mode, submission, submissionPath, permissions)
    }

    private fun processSubmissionAttachedFiles(
        mode: FileMode,
        submission: ExtSubmission,
        path: Path,
        filePermissions: Set<PosixFilePermission>
    ) {
        when (mode) {
            MOVE -> processFiles(submission, path, filePermissions, this::move)
            COPY -> processFiles(submission, path, filePermissions, this::copy)
        }
    }

    private fun permissions(released: Boolean): Set<PosixFilePermission> = if (released) ALL_READ else READ_ONLY_GROUP

    private fun generatePageTab(
        submission: ExtSubmission,
        submissionPath: Path,
        permissions: Set<PosixFilePermission>
    ) {
        generatePageTab(submission.toSimpleSubmission(), submissionPath, submission.accNo, permissions)
        submission.allFileList.forEach { generatePageTab(it.toFilesTable(), submissionPath, it.fileName, permissions) }
    }

    private fun <T> generatePageTab(
        element: T,
        submissionPath: Path,
        fileName: String,
        permissions: Set<PosixFilePermission>
    ) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)

        FileUtils.writeContent(submissionPath.resolve("$fileName.json").toFile(), json, permissions)
        FileUtils.writeContent(submissionPath.resolve("$fileName.xml").toFile(), xml, permissions)
        FileUtils.writeContent(submissionPath.resolve("$fileName.pagetab.tsv").toFile(), tsv, permissions)
    }

    private fun processFiles(
        submission: ExtSubmission,
        submissionPath: Path,
        permissions: Set<PosixFilePermission>,
        processFile: (ExtFile, File, Set<PosixFilePermission>) -> Unit
    ) {
        val submissionFolder = getSubmissionFolder(submissionPath)
        val temporally = createTempFolder(submissionFolder, submission.accNo)
        val filesPath = submissionFolder.resolve(FILES_PATH)
        val allSubmissionFiles = getMovingFiles(submission)

        allSubmissionFiles.forEach { processFile(it, temporally, permissions) }

        deleteFile(filesPath)
        moveFile(temporally, filesPath, permissions)
    }

    private fun getMovingFiles(submission: ExtSubmission): List<ExtFile> =
        (submission.allFiles + submission.allReferencedFiles).distinctBy { it.file }

    private fun copy(extFile: ExtFile, file: File, permissions: Set<PosixFilePermission>) =
        copyOrReplaceFile(extFile.file, file.resolve(extFile.fileName), permissions)

    private fun move(file: ExtFile, path: File, permissions: Set<PosixFilePermission>) =
        moveFile(file.file, path.resolve(file.fileName), permissions)

    private fun getSubmissionFolder(submissionPath: Path): File =
        getOrCreateFolder(submissionPath, READ_ONLY_GROUP).toFile()

    private fun createTempFolder(submissionFolder: File, accNo: String): File =
        reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), ONLY_USER)
}
