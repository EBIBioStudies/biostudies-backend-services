package ac.uk.ebi.biostd.exception

class InvalidFileListException(
    private val fileName: String,
    private val errorMessage: String,
) : RuntimeException() {
    override val message: String
        get() = "Problem processing file list '$fileName': $errorMessage"

    companion object {
        fun directoryCantBeFileList(fileName: String) = InvalidFileListException(fileName, "A directory can't be used as File List")

        fun emptyFileList(fileName: String) = InvalidFileListException(fileName, "A file list should contain at least one file")
    }
}
