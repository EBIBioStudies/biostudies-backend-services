package ac.uk.ebi.biostd.persistence.common.exception

class ConcurrentSubException(
    accNo: String,
    version: Int,
) : RuntimeException(
    "Submission request can't be accepted. Version '$version' of '$accNo' is currently being processed."
)
