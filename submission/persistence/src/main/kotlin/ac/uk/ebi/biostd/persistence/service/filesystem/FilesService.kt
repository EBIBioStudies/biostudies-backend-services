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
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

internal val READ_ONLY_GROUP: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-x---")
internal val ALL_CAN_READ: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-xr-x")

@Suppress("TooManyFunctions")
class FilesService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        val permissions = permissions(submission.released)
        val submissionFolder = getOrCreateSubmissionFolder(submission, permissions)

        generatePageTab(submission, submissionFolder, permissions)
        processSubmissionAttachedFiles(mode, submission, submissionFolder, permissions)
    }

    private fun processSubmissionAttachedFiles(
        mode: FileMode,
        submission: ExtSubmission,
        submissionFolder: File,
        filePermissions: Set<PosixFilePermission>
    ) {
        when (mode) {
            MOVE -> processFiles(submission, submissionFolder, filePermissions, this::move)
            COPY -> processFiles(submission, submissionFolder, filePermissions, this::copy)
        }
    }

    private fun permissions(released: Boolean): Set<PosixFilePermission> =
        if (released) ALL_CAN_READ else READ_ONLY_GROUP

    private fun generatePageTab(
        submission: ExtSubmission,
        submissionPath: File,
        permissions: Set<PosixFilePermission>
    ) {
        generatePageTab(submission.toSimpleSubmission(), submissionPath, submission.accNo, permissions)
        submission.allFileList.forEach { generatePageTab(it.toFilesTable(), submissionPath, it.fileName, permissions) }
    }

    private fun <T> generatePageTab(
        element: T,
        submissionPath: File,
        fileName: String,
        permissions: Set<PosixFilePermission>
    ) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)

        FileUtils.writeContent(submissionPath.resolve("$fileName.json"), json, permissions)
        FileUtils.writeContent(submissionPath.resolve("$fileName.xml"), xml, permissions)
        FileUtils.writeContent(submissionPath.resolve("$fileName.pagetab.tsv"), tsv, permissions)
    }

    private fun processFiles(
        submission: ExtSubmission,
        submissionFolder: File,
        permissions: Set<PosixFilePermission>,
        processFile: (ExtFile, File, Set<PosixFilePermission>) -> Unit
    ) {
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

    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
        val submissionPath = folderResolver.getSubmissionFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, ALL_CAN_READ)
        return getOrCreateFolder(submissionPath, permissions).toFile()
    }

    private fun createTempFolder(submissionFolder: File, accNo: String): File =
        reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), ONLY_USER)
}
