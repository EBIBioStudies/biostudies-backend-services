package ac.uk.ebi.transpiler.exception

class InvalidDirectoryException(private val path: String) : RuntimeException() {
    override val message: String?
        get() = "No files found for path $path"
}
