package ac.uk.ebi.biostd.exception

class InvalidFileListException(
    fileName: String,
    message: String
) : RuntimeException("Problem processing file list '$fileName': $message") {
    companion object {
        fun directoryCantBeFileList(fileName: String) =
            InvalidFileListException(fileName, "A directory can't be used as File List")

        fun emptyFileList(fileName: String) =
            InvalidFileListException(fileName, "A file list should contain at least one file")
    }
}
