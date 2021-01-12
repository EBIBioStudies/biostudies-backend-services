package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionAttribute

val DbSubmission.validAttributes: List<DbSubmissionAttribute>
    get() = attributes.filterNot { it.value.isBlank() }
