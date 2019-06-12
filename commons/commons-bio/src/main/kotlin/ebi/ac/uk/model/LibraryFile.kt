package ebi.ac.uk.model

data class LibraryFile(var name: String, val referencedFiles: List<File> = emptyList())
