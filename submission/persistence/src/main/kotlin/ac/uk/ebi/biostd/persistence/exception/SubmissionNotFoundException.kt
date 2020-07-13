package ac.uk.ebi.biostd.persistence.exception

class SubmissionNotFoundException(accNo: String) : RuntimeException("The submission '$accNo' was not found")
