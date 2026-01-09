package ac.uk.ebi.biostd.files.exception

class FileOperationException(
    operation: String,
    fileName: String,
) : RuntimeException() {
    override val message: String = "Failed to $operation file '$fileName'"
}
