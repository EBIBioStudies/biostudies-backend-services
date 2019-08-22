package ebi.ac.uk.test

import io.github.glytching.junit.extension.folder.TemporaryFolder
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

fun TemporaryFolder.replaceFile(fileName: String): File {
    val file = root.resolve(fileName)
    if (file.exists()) file.delete()
    return createFile(fileName)
}
