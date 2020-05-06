package uk.ac.ebi.stats.mapping

import uk.ac.ebi.stats.model.SubmissionStat
import uk.ac.ebi.stats.persistence.model.SubmissionStatDb

object SubmissionStatMapper {
    fun toSubmissionStat(submissionStatDb: SubmissionStatDb): SubmissionStat =
        SubmissionStat(submissionStatDb.accNo, submissionStatDb.value, submissionStatDb.type)

    fun toSubmissionStatDb(submissionStat: SubmissionStat): SubmissionStatDb =
        SubmissionStatDb(accNo = submissionStat.accNo, value = submissionStat.value, type = submissionStat.type)
}
