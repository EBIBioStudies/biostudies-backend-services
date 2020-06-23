package ac.uk.ebi.biostd.persistence.exception

class SubmissionNotFoundException(accNo: String) : RuntimeException("The submission '$accNo' was not found")

class SubmissionsNotFoundException(
    accNo: List<String>
) : RuntimeException("The following submissions were not found: ${accNo.joinToString(", ")}")
