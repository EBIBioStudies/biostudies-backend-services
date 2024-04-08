package uk.ac.ebi.serialization.common

import ebi.ac.uk.io.ext.createTempFile
import ebi.ac.uk.io.ext.resolveMany
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.YEAR

class FilesResolver(private val basePath: File) {
    fun createExtEmptyFile(
        subAccNo: String,
        version: Int,
        fileName: String,
    ): File {
        val name = fileName.replace("/", "-")
        return baseFolder(subAccNo, version.toString()).createTempFile(name, ".json")
    }

    fun createEmptyFile(fileName: String): File {
        val name = fileName.replace("/", "-")
        return baseFolder("submissions").createTempFile(name, ".json")
    }

    private fun baseFolder(vararg keys: String): File {
        val now = LocalDate.now()
        val path =
            basePath
                .resolve(now.get(YEAR).toString())
                .resolve(now.get(MONTH_OF_YEAR).toString())
                .resolve(now.get(DAY_OF_MONTH).toString())
                .resolveMany(*keys)
        path.mkdirs()
        return path
    }
}
