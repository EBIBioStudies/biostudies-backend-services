package ac.uk.ebi.biostd.submission.exceptions

class ConcurrentSubException(
    accNo: String,
) : RuntimeException("Submission request can't be accepted. Another version for '$accNo' is currently being processed.")
