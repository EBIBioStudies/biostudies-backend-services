package ebi.ac.uk.errors

class InvalidPathException(
    private val path: String,
    private val fileListName: String? = null,
) : RuntimeException() {
    override val message: String
        get() =
            buildString {
                append("The given file path contains invalid characters: $path\n")
                fileListName?.let { append("Referenced in file list: $it\n") }
                append("For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list")
            }
}
