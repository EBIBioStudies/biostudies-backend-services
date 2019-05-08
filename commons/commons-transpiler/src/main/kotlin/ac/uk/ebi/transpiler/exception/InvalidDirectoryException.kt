package ac.uk.ebi.transpiler.exception

import java.io.File

class InvalidDirectoryException(private val directories: List<File>) : RuntimeException() {
    override val message: String?
        get() = "The following directories don't exist or don't contain files: $directories"
}
