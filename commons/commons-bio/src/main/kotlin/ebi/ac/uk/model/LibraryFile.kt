package ebi.ac.uk.model

class LibraryFile(var name: String, val referencedFiles: MutableSet<File> = mutableSetOf()) {
    fun addFile(file: File) = referencedFiles.add(file)
}
