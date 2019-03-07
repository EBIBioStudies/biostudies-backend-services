package ebi.ac.uk.model

class LibraryFile(var name: String, val referencedFiles: MutableList<File> = mutableListOf()) {
    fun addFile(file: File) = referencedFiles.add(file)
}
