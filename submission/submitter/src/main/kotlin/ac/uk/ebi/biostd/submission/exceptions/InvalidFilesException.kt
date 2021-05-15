package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.File

/**
 * Generated when a submission includes a reference to a file which is not present.
 */
class InvalidFilesException(private val invalidFiles: List<File>) : RuntimeException() {

    override val message: String
        get() = invalidFiles.joinToString(
            prefix = (if (invalidFiles.size == 1) "File" else "Files").plus(" not uploaded: "),
            separator = ", "
        ) { it.path }
}
