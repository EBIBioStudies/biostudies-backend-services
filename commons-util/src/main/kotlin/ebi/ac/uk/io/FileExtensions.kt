package ebi.ac.uk.io

import java.io.File

fun File.notExist() = this.exists().not()

fun File.asFileList() = if (isDirectory) listFiles().toList() else listOf(this)
