package ac.uk.ebi.biostd.submission.exceptions

class PastReleaseDateException : RuntimeException("Release date cannot be in the past")

class InvalidReleaseException : RuntimeException("The release date of a public study cannot be changed")

class UnreleasedSubmissionException : RuntimeException("Can't generate FTP links for a private submission")
