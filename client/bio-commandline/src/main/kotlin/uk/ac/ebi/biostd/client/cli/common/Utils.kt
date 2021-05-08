package uk.ac.ebi.biostd.client.cli.common

import java.io.File

fun getFiles(file: File): List<File> =
    if (file.isDirectory) file.walk().filter { it.isFile }.toList() else listOf(file)
