package ac.uk.ebi.biostd.persistence.service.filesystem

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

private val ALL_READ: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-xr-x")

class FtpFilesService(private val folderResolver: SubmissionFolderResolver) {

    fun createFtpFolder(submission: ExtSubmission) {
        val submissionFolder = folderResolver.getSubmissionFolder(submission.relPath).toFile()
        val ftpFolder = getFtpFolder(submission)
        FileUtils.createHardLink(submissionFolder, ftpFolder)
    }

    private fun getFtpFolder(submission: ExtSubmission): File =
        FileUtils.getOrCreateFolder(
            folderResolver.getSubmissionFtpFolder(submission),
            ALL_READ
        ).toFile()

    fun cleanFtpFolder(submission: ExtSubmission) {
        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(submission).toFile())
    }
}
