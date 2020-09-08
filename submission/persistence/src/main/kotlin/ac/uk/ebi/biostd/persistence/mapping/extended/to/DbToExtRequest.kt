package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionStat

data class DbToExtRequest(val submission: DbSubmission, val stats: List<DbSubmissionStat>)
