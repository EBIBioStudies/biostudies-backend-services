package ac.uk.ebi.biostd.files.exception

class FileAlreadyExistsException(
    path: String,
    fileName: String,
) : RuntimeException() {
    override val message: String = "File '$fileName' already exists at path '$path'"
}
