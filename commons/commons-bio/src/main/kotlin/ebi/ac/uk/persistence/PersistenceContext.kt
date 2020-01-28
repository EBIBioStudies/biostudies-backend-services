package ebi.ac.uk.persistence

import arrow.core.Option
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import java.time.OffsetDateTime

@Suppress("TooManyFunctions")
interface PersistenceContext {
    fun createAccNoPatternSequence(pattern: String)

    fun getSequenceNextValue(pattern: String): Long

    fun hasParent(submission: ExtendedSubmission): Boolean

    fun getParentAccessTags(submission: Submission): List<String>

    fun getParentAccPattern(submission: Submission): Option<String>

    fun getParentReleaseTime(submission: Submission): OffsetDateTime?

    fun getSubmission(accNo: String): ExtendedSubmission?

    fun saveSubmission(submission: ExtendedSubmission): Submission

    fun isNew(accNo: String): Boolean

    fun saveAccessTag(accessTag: String)

    fun accessTagExists(accessTag: String): Boolean

    fun deleteSubmissionDrafts(submission: ExtendedSubmission)
}
