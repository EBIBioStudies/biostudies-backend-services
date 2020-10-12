package ac.uk.ebi.biostd.persistence.service.filesystem

import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File

class FtpFilesService(private val folderResolver: SubmissionFolderResolver) {
    fun createFtpFolder(relPath: String) {
        val submissionFolder = folderResolver.getSubFolder(relPath).toFile()
        val ftpFolder = getFtpFolder(relPath)

        FileUtils.createHardLink(submissionFolder, ftpFolder, RW_R__R__, RWXR_XR_X)
    }

    private fun getFtpFolder(relPath: String): File =
        FileUtils.getOrCreateFolder(folderResolver.getSubmissionFtpFolder(relPath), RWXR_XR_X).toFile()

    fun cleanFtpFolder(relPath: String) {
        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(relPath).toFile())
    }
}
