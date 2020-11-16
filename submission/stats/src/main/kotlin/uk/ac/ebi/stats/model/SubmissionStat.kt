package uk.ac.ebi.stats.model

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType

data class SubmissionStat(
    val accNo: String,
    val value: Long,
    val type: SubmissionStatType
)
