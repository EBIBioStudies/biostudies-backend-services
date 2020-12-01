package ac.uk.ebi.biostd.persistence.exception

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType

class StatNotFoundException(
    accNo: String,
    type: SubmissionStatType
) : RuntimeException("There is no submission stat registered with AccNo $accNo and type $type")
