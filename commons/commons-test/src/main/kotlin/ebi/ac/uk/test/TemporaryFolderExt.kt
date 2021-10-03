package ebi.ac.uk.test

import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset

/**
 * Creates a file with the given content in the temporary folder.
 */
fun TemporaryFolder.createFile(fileName: String, content: String, charset: Charset = Charsets.UTF_8): File {
    val file = createFile(fileName)
    file.writeText(content, charset)
    return file
}

/**
 * Creates a file with the given name or replaces it if already exist.
 */
fun TemporaryFolder.createOrReplaceFile(fileName: String): File {
    val file = root.resolve(fileName)
    if (file.exists()) file.delete()
    return createFile(fileName)
}

/**
 * Creates a file with the given name and content or replaces it if already exist.
 */
fun TemporaryFolder.createOrReplaceFile(fileName: String, content: String): File {
    val file = root.resolve(fileName)
    if (file.exists()) file.delete()
    return createFile(fileName, content)
}

/**
 * Creates a directory with the given name or replaces it if already exist.
 */
fun TemporaryFolder.createOrReplaceDirectory(directoryName: String): File {
    val file = root.resolve(directoryName)
    if (file.exists()) FileUtils.deleteDirectory(file)
    return createDirectory(directoryName)
}

/**
 * Delete all files in the temporally folder.
 */
fun TemporaryFolder.clean(): Unit = FileUtils.cleanDirectory(root)
