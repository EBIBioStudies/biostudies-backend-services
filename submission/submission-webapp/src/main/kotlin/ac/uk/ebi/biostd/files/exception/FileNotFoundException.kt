package ac.uk.ebi.biostd.files.exception

class FileNotFoundException(
    path: String,
    fileName: String,
) : RuntimeException() {
    override val message: String = "File '$fileName' does not exist at path '$path'"
}
