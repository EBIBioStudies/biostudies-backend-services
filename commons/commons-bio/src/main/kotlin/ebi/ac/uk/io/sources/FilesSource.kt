package ebi.ac.uk.io.sources

import java.io.File

interface FilesSource {
    fun exists(filePath: String): Boolean

    fun getFile(filePath: String): File

    fun readText(filePath: String): String
}
