package ac.uk.ebi.biostd.submission.model

import arrow.core.Option
import ebi.ac.uk.model.IAccessTag
import ebi.ac.uk.model.ISubmission

interface PersistenceContext {

    fun getSequenceNextValue(name: String): Long

    fun getParentAccessTags(submissionDb: ISubmission): List<IAccessTag>

    fun getParentAccPattern(submissionDb: ISubmission): Option<String>
}
