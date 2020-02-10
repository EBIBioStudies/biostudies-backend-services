package ac.uk.ebi.biostd.persistence.integration

import arrow.core.Option
import ebi.ac.uk.extended.model.ExtSubmission
import java.time.OffsetDateTime

@Suppress("TooManyFunctions")
interface PersistenceContext {
    fun createAccNoPatternSequence(pattern: String)

    fun getSequenceNextValue(pattern: String): Long

    fun getParentAccPattern(parentAccNo: String): Option<String>

    fun isNew(accNo: String): Boolean

    fun saveAccessTag(accessTag: String)

    fun accessTagExists(accessTag: String): Boolean

    fun deleteSubmissionDrafts(userId: Long, accNo: String)

    fun getSecret(accNo: String): String

    fun getAccessTags(accNo: String): List<String>

    fun getReleaseTime(accNo: String): OffsetDateTime?

    fun existByAccNo(accNo: String): Boolean

    fun getNextVersion(accNo: String): Int

    fun findCreationTime(accNo: String): OffsetDateTime?

    fun saveSubmission(submission: ExtSubmission, submitter: String, submitterId: Long): ExtSubmission

    fun getAuthor(accNo: String): String
}
