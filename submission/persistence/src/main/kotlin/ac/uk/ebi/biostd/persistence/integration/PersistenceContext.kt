package ac.uk.ebi.biostd.persistence.integration

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.User

@Suppress("TooManyFunctions")
interface PersistenceContext {
    fun createAccNoPatternSequence(pattern: String)

    fun getSequenceNextValue(pattern: String): Long

    fun saveAccessTag(accessTag: String)

    fun accessTagExists(accessTag: String): Boolean

    fun deleteSubmissionDrafts(userId: Long, accNo: String)

    fun getNextVersion(accNo: String): Int

    fun saveSubmission(saveRequest: SaveRequest): ExtSubmission

    fun refreshSubmission(submission: ExtSubmission, submitter: User)
}
