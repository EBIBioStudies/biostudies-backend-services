package ac.uk.ebi.biostd.persistence.service.filesystem

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.FileMode.MOVE
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.extended.model.allReferencedFiles
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.deleteFile
import ebi.ac.uk.io.FileUtils.getOrCreateFolder
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.FileUtils.reCreateFolder
import ebi.ac.uk.io.FileUtils.writeContent
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RWX______
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class FilesService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        logger.info { "Starting processing files of submission ${submission.accNo}" }
        val filePermissions = filePermissions(submission.released)
        val folderPermissions = folderPermissions(submission.released)
        val submissionFolder = getOrCreateSubmissionFolder(submission, folderPermissions)

        generatePageTab(submission, submissionFolder, filePermissions, folderPermissions)
        processAttachedFiles(mode, submission, submissionFolder, filePermissions, folderPermissions)
        logger.info { "Finishing processing file of submission ${submission.accNo}" }
    }

    private fun processAttachedFiles(
        mode: FileMode,
        submission: ExtSubmission,
        submissionFolder: File,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        logger.info { "processing submission ${submission.accNo} files in $mode" }
        when (mode) {
            MOVE -> processFiles(submission, submissionFolder, filePermissions, folderPermissions, this::move)
            COPY -> processFiles(submission, submissionFolder, filePermissions, folderPermissions, this::copy)
        }
        logger.info { "Finishing processing submission ${submission.accNo} files in $mode" }
    }

    private fun filePermissions(released: Boolean) = if (released) RW_R__R__ else RW_R_____

    private fun folderPermissions(released: Boolean) = if (released) RWXR_XR_X else RWXR_X___

    private fun generatePageTab(
        submission: ExtSubmission,
        submissionFolder: File,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        val accNo = submission.accNo
        logger.info { "generating submission $accNo pagetab files" }

        generatePageTab(submission.toSimpleSubmission(), submissionFolder, accNo, filePermissions, folderPermissions)
        submission.allFileList.forEach {
            generatePageTab(it.toFilesTable(), submissionFolder, it.fileName, filePermissions, folderPermissions)
        }

        logger.info { "pagetab generated successfully for submission $accNo" }
    }

    private fun <T> generatePageTab(
        element: T,
        submissionFolder: File,
        fileName: String,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)

        writeContent(submissionFolder.resolve("$fileName.json"), json, filePermissions, folderPermissions)
        writeContent(submissionFolder.resolve("$fileName.xml"), xml, filePermissions, folderPermissions)
        writeContent(submissionFolder.resolve("$fileName.pagetab.tsv"), tsv, filePermissions, folderPermissions)
    }

    private fun processFiles(
        submission: ExtSubmission,
        submissionFolder: File,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>,
        processFile: (ExtFile, File, File, Set<PosixFilePermission>, Set<PosixFilePermission>) -> Unit
    ) {
        val temporary = createTempFolder(submissionFolder, submission.accNo)
        val filesPath = submissionFolder.resolve(FILES_PATH)
        val allSubmissionFiles = getMovingFiles(submission)

        allSubmissionFiles.forEach { processFile(it, temporary, filesPath, filePermissions, folderPermissions) }

        deleteFile(filesPath)
        moveFile(temporary, filesPath, filePermissions, folderPermissions)
    }

    private fun getMovingFiles(submission: ExtSubmission): List<ExtFile> =
        (submission.allFiles + submission.allReferencedFiles).distinctBy { it.file }

    private fun copy(
        extFile: ExtFile,
        tempFolder: File,
        submissionFilesFolder: File,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        val source = extFile.file
        val target = tempFolder.resolve(extFile.fileName)
        val current = submissionFilesFolder.resolve(extFile.fileName)

        if (current.notExist() || source.md5() != current.md5()) {
            logger.info { "copying file ${source.absolutePath} into ${target.absolutePath}" }
            copyOrReplaceFile(source, target, filePermissions, folderPermissions)
        } else {
            moveFile(current, target, filePermissions, folderPermissions)
        }
    }

    private fun move(
        extFile: ExtFile,
        tempFolder: File,
        submissionFilesFolder: File,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        val source = extFile.file
        val target = tempFolder.resolve(extFile.fileName)

        logger.info { "moving file ${source.absolutePath} into ${target.absolutePath}" }
        moveFile(source, target, filePermissions, folderPermissions)
    }

    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Set<PosixFilePermission>): File {
        val submissionPath = folderResolver.getSubFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, RWXR_XR_X)
        return getOrCreateFolder(submissionPath, permissions).toFile()
    }

    private fun createTempFolder(submissionFolder: File, accNo: String): File =
        reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), RWX______)
}
