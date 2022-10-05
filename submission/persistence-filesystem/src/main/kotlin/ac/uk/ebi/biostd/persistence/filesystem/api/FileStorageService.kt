package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode

interface FileStorageService {
    fun releaseSubmissionFile(file: ExtFile, subRelPath: String, mode: StorageMode)

    fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission

    fun cleanSubmissionFiles(sub: ExtSubmission)

    fun generatePageTab(sub: ExtSubmission): ExtSubmission
}
