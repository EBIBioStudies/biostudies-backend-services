package ebi.ac.uk.test

import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset

/**
 * Create a file with the given content in the temporally folder.
 */
fun TemporaryFolder.createFile(fileName: String, content: String, charset: Charset = Charsets.UTF_8): File {
    val file = createFile(fileName)
    file.writeText(content, charset)
    return file
}

/**
 * Creates a file with the given name or replace current if already exist.
 */
fun TemporaryFolder.createNewFile(fileName: String): File {
    val file = root.resolve(fileName)
    if (file.exists()) file.delete()
    return createFile(fileName)
}

/**
 * Delete all files in the temporally folder.
 */
fun TemporaryFolder.clean(): Unit = FileUtils.cleanDirectory(root)
