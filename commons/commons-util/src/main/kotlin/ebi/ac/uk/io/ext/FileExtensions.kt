package ebi.ac.uk.io.ext

import ebi.ac.uk.io.FileUtils
import java.io.File
import java.io.FileOutputStream
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

fun File.gZipTo(target: File) {
    FileOutputStream(target).use { GZIPOutputStream(it).bufferedWriter().use { writer -> writer.write(readText()) } }
}
