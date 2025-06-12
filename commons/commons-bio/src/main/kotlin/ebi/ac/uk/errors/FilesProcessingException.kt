package ebi.ac.uk.errors

import ebi.ac.uk.io.sources.FileSourcesList

/**
 * Generated when a submission includes a reference to a file which is not present.
 */
class FilesProcessingException(
    private val invalidFiles: List<String>,
    private val sources: FileSourcesList,
    private val fileListName: String? = null,
) : RuntimeException() {
    constructor(
        file: String,
        sources: FileSourcesList,
        fileListName: String? = null,
    ) : this(listOf(file), sources, fileListName)

    override val message: String
        get() =
            buildString {
                append("The following files could not be found:\n")
                append(invalidFiles.joinToString(separator = "\n", postfix = "\n") { "  - $it" })
                fileListName?.let { append("Referenced in file list: $it\n") }
                append("List of available sources:\n")
                append(sources.sourcesDescription())
            }
}
