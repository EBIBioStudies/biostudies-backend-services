package ebi.ac.uk.io

import java.io.File

interface FilesSource {
    fun exists(filePath: String): Boolean

    fun getFile(filePath: String): File
}
