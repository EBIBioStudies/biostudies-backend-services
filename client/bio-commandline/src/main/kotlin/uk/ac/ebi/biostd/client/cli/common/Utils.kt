package uk.ac.ebi.biostd.client.cli.common

import ebi.ac.uk.io.sources.PreferredSource
import java.io.File

internal fun getFiles(file: File): List<File> = if (file.isDirectory) file.walk().filter { it.isFile }.toList() else listOf(file)

internal fun splitFiles(files: String?): List<File> = files?.split(LIST_SEPARATOR)?.flatMap { getFiles(File(it)) }.orEmpty()

internal fun splitPreferredSources(sources: String?): List<PreferredSource> =
    sources?.split(LIST_SEPARATOR)?.map { PreferredSource.valueOf(it) }.orEmpty()
