package ac.uk.ebi.biostd.exception

class InvalidExtensionException(private val ext: String) : RuntimeException() {
    override val message: String
        get() = "Unsupported submission or file list format $ext"
}
