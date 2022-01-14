package ebi.ac.uk.model

data class FileList(var name: String) {
    val nameWithoutExtension: String
        get() = name.substringBeforeLast(".")
}
