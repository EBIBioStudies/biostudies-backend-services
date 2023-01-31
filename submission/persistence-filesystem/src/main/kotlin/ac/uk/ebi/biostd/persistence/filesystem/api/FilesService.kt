package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission

internal interface FilesService {
    /**
     * Cleans ALL the files for the given submission.
     */
    fun deleteSubmissionFiles(sub: ExtSubmission)

    fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile

    fun deleteSubmissionFile(sub: ExtSubmission, file: ExtFile)

    fun deleteFtpLinks(sub: ExtSubmission)
}
