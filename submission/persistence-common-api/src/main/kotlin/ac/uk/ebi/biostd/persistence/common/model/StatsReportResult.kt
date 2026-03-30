package ac.uk.ebi.biostd.persistence.common.model

data class StatsReportResult(
    val result: Long,
)

data class CollectionStats(
    val count: Long,
    val filesSize: Long,
)
