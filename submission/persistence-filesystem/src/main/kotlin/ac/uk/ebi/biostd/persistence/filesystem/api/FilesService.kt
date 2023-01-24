package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission

internal interface FilesService {
    /**
     * Cleans ALL the files for the given submission.
     */
    fun cleanSubmissionFiles(sub: ExtSubmission)

    /**
     * Identifies and cleans any conflicting files on the current version so the new one can be persisted.
     */
    fun cleanCommonFiles(new: ExtSubmission, current: ExtSubmission)

    /**
     * Cleans any file present in the new version but not in the current one, hence no longer needed.
     */
    fun cleanRemainingFiles(new: ExtSubmission, current: ExtSubmission)

    fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile

    fun postProcessSubmissionFiles(sub: ExtSubmission)
}
