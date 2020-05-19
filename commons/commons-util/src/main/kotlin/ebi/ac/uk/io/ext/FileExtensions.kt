package ebi.ac.uk.io.ext

import ebi.ac.uk.io.FileUtils
import java.io.File
import java.nio.file.Files

fun File.notExist() = Files.exists(toPath()).not()

fun File.asFileList(): List<File> = FileUtils.listFiles(this)

fun File.size() = FileUtils.size(this)

fun File.createDirectory(name: String): File = Files.createDirectory(toPath().resolve(name)).toFile()

fun File.createNewFile(name: String): File = resolve(name).apply { createNewFile() }

fun File.createNewFile(name: String, text: String): File {
    val file = resolve(name)
    file.createNewFile()
    file.writeText(text)
    return file
}
