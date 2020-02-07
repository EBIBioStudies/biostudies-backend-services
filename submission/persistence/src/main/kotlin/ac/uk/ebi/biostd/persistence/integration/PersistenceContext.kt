package ac.uk.ebi.biostd.persistence.integration

import arrow.core.Option
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import java.time.OffsetDateTime

@Suppress("TooManyFunctions")
interface PersistenceContext {
    fun createAccNoPatternSequence(pattern: String)

    fun getSequenceNextValue(pattern: String): Long

    fun hasParent(submission: ExtendedSubmission): Boolean

    fun getParentAccessTags(submission: Submission): List<String>

    fun getParentAccPattern(parentAccNo: String): Option<String>

    fun getParentReleaseTime(submission: Submission): OffsetDateTime?

    fun getSubmission(accNo: String): ExtendedSubmission?

    fun saveSubmission(submission: ExtendedSubmission): Submission

    fun isNew(accNo: String): Boolean

    fun saveAccessTag(accessTag: String)

    fun accessTagExists(accessTag: String): Boolean

    fun deleteSubmissionDrafts(userId: Long, accNo: String)

    fun getSecret(accNo: String): String

    fun getAccessTags(attachTo: String): List<String>

    fun getReleaseTime(attachTo: String): OffsetDateTime?

    fun existByAccNo(attachTo: String): Boolean

    fun getNextVersion(accNo: String): Int

    fun saveSubmission(submission: ExtSubmission, submitter: String): ExtSubmission
}
