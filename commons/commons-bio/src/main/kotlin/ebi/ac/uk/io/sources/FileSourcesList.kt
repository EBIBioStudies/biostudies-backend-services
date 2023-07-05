package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.errors.InvalidPathException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

/**
 * Regex pattern used to match the suggested charset for S3 keys described at:
 * https://docs.aws.amazon.com/AmazonS3/latest/userguide/object-keys.html
 *
 * - Avoid relative paths (./ or ../)
 * - Allow any alphanumeric character (a-z | A-Z | 0-9)
 * - Allow any of the following special characters:
 *     - Exclamation point (!)
 *     - Hyphen (-)
 *     - Underscore (_)
 *     - Period (.)
 *     - Asterisk (*)
 *     - Single quote (')
 *     - Open parenthesis ( ( )
 *     - Close parenthesis ( ) )
 */
private val validPathPattern = "^(?!.*\\./)[0-9A-Za-z!-_*'(). ]+\$".toRegex()

@JvmInline
value class FileSourcesList(val sources: List<FilesSource>) {
    fun findExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile? {
        require(validPathPattern.matches(path)) { throw InvalidPathException(path) }
        return sources.firstNotNullOfOrNull { it.getExtFile(path, type, attributes) }
    }

    fun getExtFile(path: String, type: String, attributes: List<Attribute>): ExtFile {
        return findExtFile(path, type, attributes) ?: throw FilesProcessingException(path, this)
    }

    fun getFileList(path: String): File? {
        return sources.firstNotNullOfOrNull { it.getFileList(path) }
    }
}
