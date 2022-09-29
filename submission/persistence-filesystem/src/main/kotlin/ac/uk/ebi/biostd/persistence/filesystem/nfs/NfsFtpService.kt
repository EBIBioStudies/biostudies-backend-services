package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.paths.SubmissionFolderResolver
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

class NfsFtpService(
    private val folderResolver: SubmissionFolderResolver,
) {
    fun releaseSubmissionFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started processing FTP links over NFS" }
        generateLinks(sub.relPath)
        logger.info { "${sub.accNo} ${sub.owner} Finished processing FTP links over NFS" }
    }

    private fun generateLinks(relPath: String) {
        val submissionFolder = folderResolver.getSubFolder(relPath).toFile()
        val ftpFolder = getFtpFolder(relPath)
        FileUtils.createHardLink(submissionFolder, ftpFolder, Permissions(RW_R__R__, RWXR_XR_X))
    }

    private fun getFtpFolder(relPath: String): File =
        FileUtils.getOrCreateFolder(folderResolver.getSubmissionFtpFolder(relPath), RWXR_XR_X).toFile()
}
