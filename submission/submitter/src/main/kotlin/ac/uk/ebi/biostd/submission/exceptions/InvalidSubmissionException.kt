package ac.uk.ebi.biostd.submission.exceptions

class InvalidSubmissionException(message: String, val causes: MutableList<Throwable>) : RuntimeException(message)

