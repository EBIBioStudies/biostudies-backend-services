package ac.uk.ebi.biostd.exception

class InvalidFileListException(
    fileName: String,
    message: String
) : RuntimeException("Problem processing file list '$fileName': $message")
