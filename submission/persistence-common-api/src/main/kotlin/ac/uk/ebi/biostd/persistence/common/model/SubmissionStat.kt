package ac.uk.ebi.biostd.persistence.common.model

data class SubmissionStat(
    val accNo: String,
    val value: Long,
    val type: SubmissionStatType,
) {
    constructor(
        accNo: String,
        type: String,
        value: Long,
    ) : this(accNo, value, SubmissionStatType.fromString(type))
}

data class SubmissionStats(
    val accNo: String,
    val stats: List<SubmissionStat>,
)
