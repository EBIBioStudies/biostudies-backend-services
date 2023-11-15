package ebi.ac.uk.errors

class InvalidPathException(val path: String) : RuntimeException() {
    override val message: String
        get() = """
            The given file path contains invalid characters: $path
            For more information check https://www.ebi.ac.uk/bioimage-archive/help-file-list
        """
}
