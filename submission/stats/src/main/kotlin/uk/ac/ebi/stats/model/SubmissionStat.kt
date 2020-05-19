package uk.ac.ebi.stats.model

data class SubmissionStat(
    val accNo: String,
    val value: Long,
    val type: SubmissionStatType
)
