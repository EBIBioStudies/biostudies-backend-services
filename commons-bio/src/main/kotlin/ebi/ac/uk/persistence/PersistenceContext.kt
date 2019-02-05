package ebi.ac.uk.persistence

import arrow.core.Option
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User

interface PersistenceContext {

    fun getSequenceNextValue(pattern: AccPattern): Long

    fun getParentAccessTags(submission: Submission): List<String>

    fun getParentAccPattern(submission: Submission): Option<String>

    fun getSubmission(accNo: String): Option<ExtendedSubmission>

    fun saveSubmission(submission: ExtendedSubmission)

    fun canUserProvideAccNo(user: User): Boolean

    fun canSubmit(accNo: String, user: User): Boolean

    fun isNew(submission: ExtendedSubmission): Boolean
}
