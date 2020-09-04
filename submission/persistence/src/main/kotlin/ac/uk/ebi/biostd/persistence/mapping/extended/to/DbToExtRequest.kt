package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbSubmission

data class DbToExtRequest(val submission: DbSubmission, val project: List<DbSubmission>)
