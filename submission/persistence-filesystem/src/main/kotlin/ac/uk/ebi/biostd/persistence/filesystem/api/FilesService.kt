package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission

internal interface FilesService {
    fun preProcessSubmissionFiles(sub: ExtSubmission): FilePersistenceConfig

    fun persistSubmissionFile(file: ExtFile, config: FilePersistenceConfig): ExtFile

    fun postProcessSubmissionFiles(config: FilePersistenceConfig)

    fun cleanSubmissionFiles(sub: ExtSubmission)
}
