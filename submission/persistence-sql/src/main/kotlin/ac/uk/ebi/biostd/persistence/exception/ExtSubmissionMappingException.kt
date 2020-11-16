package ac.uk.ebi.biostd.persistence.exception

class ExtSubmissionMappingException(
    accNo: String,
    cause: String
) : RuntimeException("Error while mapping ext model for '$accNo' caused by: $cause")
