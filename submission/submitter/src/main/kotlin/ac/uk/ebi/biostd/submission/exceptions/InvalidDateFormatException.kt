package ac.uk.ebi.biostd.submission.exceptions

class InvalidDateFormatException(date: String) : RuntimeException(
    "Provided date $date could not be parsed. Expected format is YYYY-MM-DD")
