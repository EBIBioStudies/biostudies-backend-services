package ac.uk.ebi.biostd.submission.model

import arrow.core.Option
import ebi.ac.uk.model.Submission

interface PersistenceContext {

    fun getSequenceNextValue(name: String): Long

    fun getParentAccessTags(submissionDb: Submission): List<String>

    fun getParentAccPattern(submissionDb: Submission): Option<String>
}
