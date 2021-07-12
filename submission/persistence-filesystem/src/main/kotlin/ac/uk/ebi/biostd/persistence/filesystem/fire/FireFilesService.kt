package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode

class FireFilesService : FilesService {
    override fun persistSubmissionFiles(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        TODO("Not yet implemented")
    }
}
