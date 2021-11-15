package ac.uk.ebi.biostd.persistence.filesystem.api

import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ebi.ac.uk.extended.model.ExtSubmission

interface FilesService {
    fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission
}
