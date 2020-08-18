package ac.uk.ebi.biostd.persistence.integration

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.User

@Suppress("TooManyFunctions")
interface PersistenceContext {
    fun sequenceAccNoPatternExists(pattern: String): Boolean

    fun createAccNoPatternSequence(pattern: String)

    fun getSequenceNextValue(pattern: String): Long

    fun saveAccessTag(accessTag: String)

    fun accessTagExists(accessTag: String): Boolean

    fun saveAndProcessSubmissionRequest(saveRequest: SaveRequest): ExtSubmission

    fun saveSubmissionRequest(saveRequest: SaveRequest)

    fun processSubmission(saveRequest: SaveRequest): ExtSubmission

    fun refreshSubmission(submission: ExtSubmission, submitter: User)
}
