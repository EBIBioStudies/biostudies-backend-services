package ac.uk.ebi.biostd.persistence.common.exception

class SubmissionRequestNotFoundException(
    accNo: String,
    version: Int,
) : RuntimeException("The submission request '$accNo', version: $version was not found")
