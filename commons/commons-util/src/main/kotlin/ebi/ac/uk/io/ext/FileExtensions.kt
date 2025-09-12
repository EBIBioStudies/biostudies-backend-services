@file:Suppress("TooManyFunctions")

package ebi.ac.uk.io.ext

import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXRWX___
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.zip.GZIPOutputStream
import kotlin.text.Charsets.UTF_8

fun File.notExist() = Files.exists(toPath()).not()

fun File.notExistOrEmpty() = notExist() || isEmpty()

fun File.listFilesOrEmpty(): List<File> = FileUtils.listFiles(this)

fun File.isEmpty(): Boolean = Files.newDirectoryStream(this.toPath()).use { return it.iterator().hasNext().not() }

fun File.allSubFiles(): List<File> = FileUtils.listAllFiles(this)

fun File.size(calculateDirectories: Boolean = true) = FileUtils.size(this, calculateDirectories)

fun File.md5() = FileUtils.md5(this)

fun File.createDirectory(name: String): File = FileUtils.getOrCreateFolder(toPath().resolve(name), RWXRWX___).toFile()

fun File.newFile(name: String): File {
    val file = resolve(name)
    file.parentFile.mkdirs()
    file.createNewFile()
    return file
}

fun File.createTempFile(
    prefix: String? = null,
    suffix: String? = null,
): File = Files.createTempFile(toPath(), prefix, suffix).toFile()

fun File.createNewFile(
    name: String,
    text: String,
): File {
    val file = resolve(name)
    file.createNewFile()
    file.writeText(text)
    return file
}

fun File.gZipTo(target: File) {
    FileOutputStream(target).use { GZIPOutputStream(it).bufferedWriter().use { writer -> writer.write(readText()) } }
}

/**
 * Creates a file with the given content in the temporary folder.
 */
fun File.createFile(
    fileName: String,
    content: String,
    charset: Charset = UTF_8,
): File {
    val file = newFile(fileName)
    file.writeText(content, charset)
    return file
}

/**
 * Creates a file with the given content in the temporary folder.
 */
fun File.createFile(fileName: String): File = newFile(fileName)

/**
 * Creates a file with the given name or replaces it if already exists.
 */
fun File.createOrReplaceFile(fileName: String): File {
    val file = resolve(fileName)
    if (file.exists()) file.delete()
    return newFile(fileName)
}

/**
 * Creates a file with the given name and content or replaces it if it already exists.
 */
fun File.createOrReplaceFile(
    fileName: String,
    content: String,
    charset: Charset = UTF_8,
): File {
    val file = createFile(fileName)
    file.writeText(content, charset)
    return file
}
