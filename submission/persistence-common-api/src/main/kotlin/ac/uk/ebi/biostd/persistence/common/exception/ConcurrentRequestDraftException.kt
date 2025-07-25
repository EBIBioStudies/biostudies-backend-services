package ac.uk.ebi.biostd.persistence.common.exception

class ConcurrentRequestDraftException(
    accNo: String,
) : RuntimeException(
        "Request '$accNo' is being processed. Submission request draft operations are blocked.",
    )
