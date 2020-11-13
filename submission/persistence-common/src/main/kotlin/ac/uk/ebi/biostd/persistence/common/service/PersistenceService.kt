package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ebi.ac.uk.extended.model.ExtSubmission

@Suppress("TooManyFunctions")
interface PersistenceService {
    fun sequenceAccNoPatternExists(pattern: String): Boolean

    fun createAccNoPatternSequence(pattern: String)

    fun getSequenceNextValue(pattern: String): Long

    fun saveAccessTag(accessTag: String)

    fun accessTagExists(accessTag: String): Boolean

    fun saveAndProcessSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission

    fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission

    fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission

    fun refreshSubmission(submission: ExtSubmission)
}
