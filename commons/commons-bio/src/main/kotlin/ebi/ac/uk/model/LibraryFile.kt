package ebi.ac.uk.model

import java.util.Objects

class LibraryFile(var name: String, val referencedFiles: MutableSet<File> = mutableSetOf()) {
    fun addFile(file: File) = referencedFiles.add(file)

    override fun equals(other: Any?) = when {
        other !is LibraryFile -> false
        other === this -> true
        else -> Objects.equals(name, other.name)
            .and(Objects.equals(referencedFiles, other.referencedFiles))
    }

    override fun hashCode() = Objects.hash(name, referencedFiles)
}
