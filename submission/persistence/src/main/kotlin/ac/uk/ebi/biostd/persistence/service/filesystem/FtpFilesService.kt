package ac.uk.ebi.biostd.persistence.service.filesystem

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File

class FtpFilesService(private val folderResolver: SubmissionFolderResolver) {

    fun createFtpFolder(submission: ExtSubmission) {
        val submissionFolder = folderResolver.getSubmissionFolder(submission.relPath).toFile()
        val ftpFolder = getFtpFolder(submission)
        FileUtils.createHardLink(submissionFolder, ftpFolder)
    }

    private fun getFtpFolder(submission: ExtSubmission): File =
        folderResolver.getSubmissionFtpFolder(submission).toFile().apply { mkdirs() }

    fun cleanFtpFolder(submission: ExtSubmission) {
        FileUtils.deleteFolder(folderResolver.getSubmissionFtpFolder(submission).toFile())
    }
}
