package ac.uk.ebi.biostd.persistence.common.model

interface SubmissionStat {
    val accNo: String

    val value: Long

    val type: SubmissionStatType
}
