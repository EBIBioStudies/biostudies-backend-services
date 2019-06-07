package ebi.ac.uk.extended.integration

import java.io.File

interface FilesSource {

    fun exist(filePath: String): Boolean

    fun get(filePath: String): File
}
