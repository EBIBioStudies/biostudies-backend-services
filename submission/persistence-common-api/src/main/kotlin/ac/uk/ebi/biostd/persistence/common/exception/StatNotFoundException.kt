package ac.uk.ebi.biostd.persistence.common.exception

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType

class StatNotFoundException(
    accNo: String,
    type: SubmissionStatType,
) : RuntimeException("There is no submission stat registered with AccNo $accNo and type $type")

class StatsNotFoundException(
    accNo: String,
) : RuntimeException("There is no submission stat registered with AccNo $accNo")
