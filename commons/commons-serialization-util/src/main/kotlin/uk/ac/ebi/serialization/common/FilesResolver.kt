package uk.ac.ebi.serialization.common

import ebi.ac.uk.io.ext.createTempFile
import java.io.File
import java.nio.file.Files

class FilesResolver(private val basePath: File) {
    fun createRequestTempFile(
        subAccNo: String,
        version: Int,
        fileName: String,
    ): File {
        val name = fileName.replace("/", "-")
        val baseFolder = basePath.resolve("$subAccNo/$version")
        baseFolder.mkdirs()

        return baseFolder.createTempFile(name, ".json")
    }

    /**
     * This method is intended to create temporary json files. The files will be created in the system tmp folder and
     * will be periodically cleaned.
     */
    fun createTempFile(fileName: String): File {
        val name = fileName.replace("/", "-")
        return Files.createTempFile(name, ".json").toFile()
    }
}
