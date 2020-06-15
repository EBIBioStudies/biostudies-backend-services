package ac.uk.ebi.biostd.persistence.service.filesystem

import ac.uk.ebi.biostd.persistence.integration.FileMode
import ebi.ac.uk.extended.model.ExtSubmission

class FileSystemService(
    private val filesService: FilesService,
    private val ftpLinksService: FtpFilesService
) {
    fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode) {
        // ftpLinksService.cleanFtpFolder(submission)
        filesService.persistSubmissionFiles(submission, mode)
        // if (submission.released) ftpLinksService.createFtpFolder(submission)
    }
}
