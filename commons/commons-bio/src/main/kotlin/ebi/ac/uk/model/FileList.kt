package ebi.ac.uk.model

data class FileList(var name: String, val referencedFiles: List<File> = emptyList())

val FileList.canonicalName
    get() = name.substringBeforeLast(".")
