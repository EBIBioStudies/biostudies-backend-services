package ebi.ac.uk.errors

import ebi.ac.uk.io.sources.FileSourcesList

/**
 * Generated when a submission includes a reference to a file which is not present.
 */
class FilesProcessingException(private val invalidFiles: List<String>, private val sources: FileSourcesList) :
    RuntimeException() {

    constructor(file: String, sources: FileSourcesList) : this(listOf(file), sources)

    override val message: String
        get() = buildString {
            append("The following files could not be found:\n")
            append(invalidFiles.joinToString(separator = "\n", postfix = "\n") { "  - $it" })
            append("List of available sources:\n")
            append(sources.sources.joinToString(separator = "\n") { "  - ${it.description}" })
        }
}
