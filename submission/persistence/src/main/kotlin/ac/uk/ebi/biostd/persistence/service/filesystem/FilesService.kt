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
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.Permissions.ALL_CAN_READ
import ebi.ac.uk.io.Permissions.ONLY_USER
import ebi.ac.uk.io.Permissions.READ_ONLY_GROUP
import ebi.ac.uk.io.toPosix
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class FilesService(
    private val folderResolver: SubmissionFolderResolver,
    private val serializationService: SerializationService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        logger.info { "Starting processing files of submission ${submission.accNo}" }
        val permissions = permissions(submission.released)
        val submissionFolder = getOrCreateSubmissionFolder(submission, permissions)

        generatePageTab(submission, submissionFolder, permissions)
        processAttachedFiles(mode, submission, submissionFolder, permissions)
        logger.info { "Finishing processing file of submission ${submission.accNo}" }
    }

    private fun processAttachedFiles(
        mode: FileMode,
        submission: ExtSubmission,
        submissionFolder: File,
        filePermissions: Permissions
    ) {
        logger.info { "processing submission ${submission.accNo} files in $mode" }
        when (mode) {
            MOVE -> processFiles(submission, submissionFolder, filePermissions, this::move)
            COPY -> processFiles(submission, submissionFolder, filePermissions, this::copy)
        }
        logger.info { "Finishing processing submission ${submission.accNo} files in $mode" }
    }

    private fun permissions(released: Boolean): Permissions = if (released) ALL_CAN_READ else READ_ONLY_GROUP

    private fun generatePageTab(
        submission: ExtSubmission,
        submissionFolder: File,
        permissions: Permissions
    ) {
        logger.info { "generating submission ${submission.accNo} pagetab files" }

        generatePageTab(submission.toSimpleSubmission(), submissionFolder, submission.accNo, permissions)
        submission.allFileList.forEach {
            generatePageTab(it.toFilesTable(), submissionFolder, it.fileName, permissions)
        }

        logger.info { "pagetab generated successfully for submission ${submission.accNo}" }
    }

    private fun <T> generatePageTab(
        element: T,
        submissionFolder: File,
        fileName: String,
        permissions: Permissions
    ) {
        val json = serializationService.serializeElement(element, SubFormat.JSON_PRETTY)
        val xml = serializationService.serializeElement(element, SubFormat.XML)
        val tsv = serializationService.serializeElement(element, SubFormat.TSV)

        FileUtils.writeContent(submissionFolder.resolve("$fileName.json"), json, permissions)
        FileUtils.writeContent(submissionFolder.resolve("$fileName.xml"), xml, permissions)
        FileUtils.writeContent(submissionFolder.resolve("$fileName.pagetab.tsv"), tsv, permissions)
    }

    private fun processFiles(
        submission: ExtSubmission,
        submissionFolder: File,
        permissions: Permissions,
        processFile: (ExtFile, File, Permissions) -> Unit
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

    private fun copy(extFile: ExtFile, folder: File, permissions: Permissions) {
        val source = extFile.file
        val target = folder.resolve(extFile.fileName)

        logger.info { "copying file ${source.absolutePath} into ${target.absolutePath}" }
        copyOrReplaceFile(source, target, permissions)
    }

    private fun move(extFile: ExtFile, folder: File, permissions: Permissions) {
        val source = extFile.file
        val target = folder.resolve(extFile.fileName)

        logger.info { "moving file ${source.absolutePath} into ${target.absolutePath}" }
        moveFile(source, target, permissions)
    }

    private fun getOrCreateSubmissionFolder(submission: ExtSubmission, permissions: Permissions): File {
        val submissionPath = folderResolver.getSubFolder(submission.relPath)
        FileUtils.createParentFolders(submissionPath, ALL_CAN_READ.toPosix())
        return getOrCreateFolder(submissionPath, permissions).toFile()
    }

    private fun createTempFolder(submissionFolder: File, accNo: String): File =
        reCreateFolder(submissionFolder.parentFile.resolve("${accNo}_temp"), ONLY_USER)
}
