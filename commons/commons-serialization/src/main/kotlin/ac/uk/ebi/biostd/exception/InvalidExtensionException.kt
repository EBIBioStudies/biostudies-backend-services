package ac.uk.ebi.biostd.exception

class InvalidExtensionException(private val ext: String) : RuntimeException() {
    override val message: String
        get() = "Unsupported page tab format $ext"
}
