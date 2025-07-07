package ac.uk.ebi.biostd.persistence.common.exception

class ConcurrentRequestDraftException(
    accNo: String,
) : RuntimeException(
        "Submission request draft can't be updated. Request '$accNo' is currently being processed.",
    )
