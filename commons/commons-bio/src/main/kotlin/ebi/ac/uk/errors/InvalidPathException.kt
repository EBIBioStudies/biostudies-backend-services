package ebi.ac.uk.errors

class InvalidPathException(path: String) : RuntimeException("The given file path contains invalid characters: $path")
