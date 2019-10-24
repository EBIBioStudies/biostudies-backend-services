package ebi.ac.uk.persistence

import arrow.core.Option
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission

interface PersistenceContext {
    fun getSequenceNextValue(pattern: AccPattern): Long

    fun getParentAccessTags(submission: Submission): List<String>

    fun getParentAccPattern(submission: Submission): Option<String>

    fun getSubmission(accNo: String): ExtendedSubmission?

    fun saveSubmission(submission: ExtendedSubmission)

    fun isNew(accNo: String): Boolean

    fun saveAccessTag(accessTag: String)

    fun accessTagExists(accessTag: String): Boolean

    fun deleteSubmissionDrafts(submission: ExtendedSubmission)
}
