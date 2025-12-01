package ac.uk.ebi.biostd.persistence.common.exception

class ConcurrentSubException(
    accNo: String,
) : RuntimeException(
        "Submission request can't be accepted. Submission with '$accNo' is currently being processed.",
    )
