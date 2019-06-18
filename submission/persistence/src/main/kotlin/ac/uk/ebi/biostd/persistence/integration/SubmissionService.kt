package ac.uk.ebi.biostd.persistence.integration

import arrow.core.Option
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import java.time.OffsetDateTime

interface SubmissionService {

    fun isNew(accNo: String): Boolean
    fun getSequenceNextValue(pattern: AccPattern): Long
    fun getCreationTime(accNo: String): OffsetDateTime?

    fun getProjectAccPattern(submission: Submission): Option<String>
    fun getProjectAccessTags(accNo: String): List<String>
    fun existProject(accNo: String): Boolean

    fun canDelete(accNo: String, asUser: User): Boolean
    fun canUserProvideAccNo(user: User): Boolean
    fun canSubmit(accNo: String, user: User): Boolean
    fun canAttach(accNo: String, user: User): Boolean
    fun submit(extSubmission: ExtSubmission, user: User): ExtSubmission


}

