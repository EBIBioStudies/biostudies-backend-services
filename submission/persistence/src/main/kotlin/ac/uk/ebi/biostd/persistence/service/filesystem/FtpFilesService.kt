package ac.uk.ebi.biostd.persistence.service.filesystem

import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

private val ALL_READ: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-xr-x")

class FtpFilesService(private val folderResolver: SubmissionFolderResolver) {

    fun createFtpFolder(relPath: String) {
        val submissionFolder = folderResolver.getSubmissionFolder(relPath).toFile()
        val ftpFolder = getFtpFolder(relPath)
        FileUtils.createHardLink(submissionFolder, ftpFolder)
    }

    private fun getFtpFolder(relPath: String): File =
        FileUtils.getOrCreateFolder(
            folderResolver.getSubmissionFtpFolder(relPath),
            ALL_READ
        ).toFile()

    fun cleanFtpFolder(relPath: String) {
        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(relPath).toFile())
    }
}
