package ac.uk.ebi.biostd.submission.exceptions

class InvalidSubmissionException(message: String, val causes: List<Throwable>) : RuntimeException(message)
