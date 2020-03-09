package ebi.ac.uk.io.sources

import java.io.File

interface FilesSource {
    fun exists(filePath: String): Boolean

    fun getFile(filePath: String): File

    fun size(filePath: String): Long

    fun readText(filePath: String): String

    fun and(filesSource: FilesSource): ComposedFileSource = ComposedFileSource(listOf(this, filesSource))
}
