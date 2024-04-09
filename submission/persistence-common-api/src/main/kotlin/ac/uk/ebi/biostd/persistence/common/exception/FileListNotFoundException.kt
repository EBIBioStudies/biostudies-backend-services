package ac.uk.ebi.biostd.persistence.common.exception

class FileListNotFoundException(
    accNo: String,
    fileListName: String,
) : RuntimeException("The file list '$fileListName' could not be found in the submission '$accNo'")
