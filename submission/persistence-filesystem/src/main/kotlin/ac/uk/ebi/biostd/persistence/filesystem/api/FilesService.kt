package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission

internal interface FilesService {
    fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): ExtFile

    fun deleteSubmissionFiles(sub: ExtSubmission)

    fun deleteSubmissionFile(sub: ExtSubmission, file: ExtFile)

    fun deleteFtpFile(sub: ExtSubmission, file: ExtFile)
}
