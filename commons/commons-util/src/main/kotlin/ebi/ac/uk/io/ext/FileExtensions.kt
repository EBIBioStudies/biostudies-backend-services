@file:Suppress("TooManyFunctions")

package ebi.ac.uk.io.ext

import ebi.ac.uk.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.zip.GZIPOutputStream

fun File.notExist() = Files.exists(toPath()).not()

fun File.asFileList(): List<File> = FileUtils.listFiles(this)

fun File.size() = FileUtils.size(this)

fun File.md5() = FileUtils.md5(this)

fun File.createDirectory(name: String): File = Files.createDirectory(toPath().resolve(name)).toFile()

fun File.createNewFile(name: String): File = resolve(name).apply { createNewFile() }

fun File.createTempFile(prefix: String, suffix: String): File = Files.createTempFile(toPath(), prefix, suffix).toFile()

fun File.createNewFile(name: String, text: String): File {
    val file = resolve(name)
    file.createNewFile()
    file.writeText(text)
    return file
}

fun File.resolveMany(vararg keys: String): File {
    var file: File = this
    for (key in keys) {
        file = file.resolve(key)
    }

    return file
}

fun File.gZipTo(target: File) {
    FileOutputStream(target).use { GZIPOutputStream(it).bufferedWriter().use { writer -> writer.write(readText()) } }
}

/**
 * Creates a file with the given content in the temporary folder.
 */
fun File.createFile(fileName: String, content: String, charset: Charset = Charsets.UTF_8): File {
    val file = createNewFile(fileName)
    file.writeText(content, charset)
    return file
}

/**
 * Creates a file with the given content in the temporary folder.
 */
fun File.createFile(fileName: String): File {
    return createNewFile(fileName)
}

/**
 * Creates a file with the given name or replaces it if already exist.
 */
fun File.createOrReplaceFile(fileName: String): File {
    val file = resolve(fileName)
    if (file.exists()) file.delete()
    return createNewFile(fileName)
}
