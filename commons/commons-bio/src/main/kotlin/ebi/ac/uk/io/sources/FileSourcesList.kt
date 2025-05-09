package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.errors.InvalidPathException
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.RequestFile
import java.io.File

/**
 * Regex pattern used to match the suggested charset for S3 keys described at:
 * https://docs.aws.amazon.com/AmazonS3/latest/userguide/object-keys.html
 *
 * - Avoid relative paths (./ or ../)
 * - Avoid trailing slashes
 * - Allow any alphanumeric character (a-z | A-Z | 0-9)
 * - Allow any of the following special characters:
 *     - Exclamation point [!]
 *     - Hyphen [-]
 *     - Underscore [_]
 *     - Period [.]
 *     - Asterisk [*]
 *     - Single quote [']
 *     - Open parenthesis [ ( ]
 *     - Close parenthesis [ ) ]
 *     - Space [ ]
 */

private val validPathPattern =
    buildString {
        append("^") // Start of the line
        append("(?!\\./)") // Negative lookahead to exclude "./"
        append("(?!.*/\$)") // Negative lookahead to exclude "/" at the end
        append("(?!.*\\.\\./)") // Negative lookahead to exclude occurrences of "../"
        append("[0-9A-Za-z!\\-_*'(). /]+") // Character set allowing specified characters
        append("\$") // End of the line
    }.trimIndent().toRegex()

interface FileSourcesList {
    suspend fun findExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile?

    suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile

    fun sourcesDescription(): String

    suspend fun getFileList(path: String): File?
}

class ByPassSourceList(
    private val fileSourcesList: FileSourcesList,
) : FileSourcesList by fileSourcesList {
    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile = RequestFile(path, attributes, type)
}

class SourcesList(
    val sources: List<FilesSource>,
) : FileSourcesList {
    override suspend fun findExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? {
        require(validPathPattern.matches(path)) { throw InvalidPathException(path) }
        return sources.firstNotNullOfOrNull { it.getExtFile(path, type, attributes) }
    }

    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile = findExtFile(path, type, attributes) ?: throw FilesProcessingException(path, this)

    override fun sourcesDescription(): String = sources.joinToString(separator = "\n") { "  - ${it.description}" }

    override suspend fun getFileList(path: String): File? {
        require(validPathPattern.matches(path)) { throw InvalidPathException(path) }
        return sources.firstNotNullOfOrNull { it.getFileList(path) }
    }
}
