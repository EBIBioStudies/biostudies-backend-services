package ac.uk.ebi.biostd.submission.exceptions

class ConcurrentProcessingSubmissionException(
    accNo: String
) : RuntimeException("Submission request can't be accepted. Another version for '$accNo' is currently being processed.")
