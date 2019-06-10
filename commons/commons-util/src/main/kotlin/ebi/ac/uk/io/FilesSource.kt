package ebi.ac.uk.io

import java.io.File

interface FilesSource {
    fun exists(filePath: String): Boolean

    fun getFile(filePath: String): File

    companion object EmptyFileSource : FilesSource {
        override fun exists(filePath: String) = false

        override fun getFile(filePath: String) = throw IllegalArgumentException()
    }
}
