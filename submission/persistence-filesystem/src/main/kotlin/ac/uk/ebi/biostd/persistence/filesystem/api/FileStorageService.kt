package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode

interface FileStorageService {
    fun releaseSubmissionFile(file: ExtFile, subRelPath: String, mode: StorageMode): ExtFile

    fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile

    fun cleanSubmissionFiles(sub: ExtSubmission)

    fun prepareSubmissionFiles(new: ExtSubmission, current: ExtSubmission?)

    fun postProcessSubmissionFiles(new: ExtSubmission, current: ExtSubmission?)
}
