package ac.uk.ebi.biostd.submission.exceptions

class InvalidDateFormatException(date: String) : RuntimeException(
    "Provided date $date could not be parsed. Expected format is YYYY-MM-DD"
)

class PastReleaseDateException : RuntimeException("Release date cannot be in the past")
