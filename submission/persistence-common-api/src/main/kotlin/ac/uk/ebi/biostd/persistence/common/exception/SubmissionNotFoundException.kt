package ac.uk.ebi.biostd.persistence.common.exception

class SubmissionNotFoundException(accNo: String) : RuntimeException("The submission '$accNo' was not found")
