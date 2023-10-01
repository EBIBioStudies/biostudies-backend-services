package ac.uk.ebi.biostd.submission.exceptions

class PastReleaseDateException : RuntimeException("Release date cannot be in the past")

class InvalidReleaseDateException : RuntimeException("The release date of a public study cannot be changed")
