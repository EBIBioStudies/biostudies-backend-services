package ebi.ac.uk.extended.integration

import java.io.File

interface FilesSource {

    fun exists(filePath: String): Boolean

    fun getFile(filePath: String): File
}
