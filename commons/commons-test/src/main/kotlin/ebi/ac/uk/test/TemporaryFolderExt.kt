package ebi.ac.uk.test

import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset
import kotlin.io.path.writeText

/**
 * Create a temp file with the given text content.
 */
fun createTempFile(content: String): File {
    val tempFile = kotlin.io.path.createTempFile()
    tempFile.writeText(content)
    return tempFile.toFile()
}

/**
 * Create a file with the given size.
 */
fun TemporaryFolder.createFile(fileName: String, sizeInBytes: Long): File {
    val file = createFile(fileName)

    val raf = RandomAccessFile(file, "rw")
    raf.setLength(sizeInBytes)
    raf.close()

    return file
}

/**
 * Creates a file with the given content in the temporary folder.
 */
fun TemporaryFolder.createFile(fileName: String, content: String, charset: Charset = Charsets.UTF_8): File {
    val file = createFile(fileName)
    file.writeText(content, charset)
    return file
}

/**
 * Creates a file with the given name and content or replaces it if already exist.
 */
fun TemporaryFolder.deleteFile(fileName: String) {
    val file = root.resolve(fileName)
    file.delete()
}

/**
 * Delete all files in the temporal folder.
 */
fun TemporaryFolder.clean(): Unit = FileUtils.cleanDirectory(root)
