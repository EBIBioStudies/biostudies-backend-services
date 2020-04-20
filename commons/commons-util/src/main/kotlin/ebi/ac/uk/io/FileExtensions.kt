package ebi.ac.uk.io

import java.io.File
import java.nio.file.Files
import kotlin.streams.toList

fun File.notExist() = Files.exists(toPath()).not()

fun File.asFileList(): List<File> = if (isDirectory) listFiles(this) else listOf(this)

fun File.size() = Files.size(toPath())

fun File.addNewFile(fileName: String): File = resolve(fileName).apply { createNewFile() }

private fun listFiles(file: File): List<File> = Files.list(file.toPath()).map { it.toFile() }.toList()
