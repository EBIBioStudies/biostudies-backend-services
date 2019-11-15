package ebi.ac.uk.errors

class FileNotFoundException(path: String) : RuntimeException("File not found: $path")
