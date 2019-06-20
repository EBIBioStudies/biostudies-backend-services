package ebi.ac.uk.utils

import java.io.File

interface FilesSource {

    fun exists(filePath: String): Boolean

    fun getFile(filePath: String): File
}
