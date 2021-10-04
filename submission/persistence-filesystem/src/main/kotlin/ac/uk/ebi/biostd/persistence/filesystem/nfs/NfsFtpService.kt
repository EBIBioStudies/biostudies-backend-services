package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File

class NfsFtpService(
    private val folderResolver: SubmissionFolderResolver,
    private val submissionQueryService: SubmissionQueryService
) : FtpService {
    override fun processSubmissionFiles(submission: ExtSubmission) {
        cleanFtpFolder(submission.relPath)
        if (submission.released) generateLinks(submission.relPath)
    }

    override fun generateFtpLinks(accNo: String) {
        val relPath = submissionQueryService.getExtByAccNo(accNo).relPath
        generateLinks(relPath)
    }

    private fun generateLinks(relPath: String) {
        val submissionFolder = folderResolver.getSubFolder(relPath).toFile()
        val ftpFolder = getFtpFolder(relPath)

        FileUtils.createHardLink(submissionFolder, ftpFolder, Permissions(RW_R__R__, RWXR_XR_X))
    }

    private fun getFtpFolder(relPath: String): File =
        FileUtils.getOrCreateFolder(folderResolver.getSubmissionFtpFolder(relPath), RWXR_XR_X).toFile()

    private fun cleanFtpFolder(relPath: String) {
        FileUtils.deleteFile(folderResolver.getSubmissionFtpFolder(relPath).toFile())
    }
}
