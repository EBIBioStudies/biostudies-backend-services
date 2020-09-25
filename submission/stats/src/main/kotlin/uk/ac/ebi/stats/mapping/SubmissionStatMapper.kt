package uk.ac.ebi.stats.mapping

import ac.uk.ebi.biostd.persistence.model.DbSubmissionStat
import uk.ac.ebi.stats.model.SubmissionStat

object SubmissionStatMapper {
    fun toSubmissionStat(dbSubmissionStat: DbSubmissionStat): SubmissionStat =
        SubmissionStat(dbSubmissionStat.accNo, dbSubmissionStat.value, dbSubmissionStat.type)

    fun toSubmissionStatDb(submissionStat: SubmissionStat): DbSubmissionStat =
        DbSubmissionStat(accNo = submissionStat.accNo, value = submissionStat.value, type = submissionStat.type)
}
