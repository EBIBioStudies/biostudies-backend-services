package uk.ac.ebi.stats.exception

import uk.ac.ebi.stats.model.SubmissionStatType

class StatNotFoundException(
    accNo: String, type: SubmissionStatType
) : RuntimeException("There is no submission stat registered with AccNo $accNo and type $type")

class StatAlreadyExistsException(
    accNo: String, type: SubmissionStatType
) : RuntimeException("There is already submission stat registered with AccNo $accNo and type $type")
