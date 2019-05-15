package ebi.ac.uk.errors

class InvalidDateFormatException(
    date: String
) : RuntimeException("Invalid date format provided for date $date. Expected format is YYYY-MM-DD")
