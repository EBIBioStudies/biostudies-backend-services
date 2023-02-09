package ac.uk.ebi.biostd.submission.domain.exceptions

class InvalidTransferTargetException : RuntimeException("The target and current storage mode must be different")
