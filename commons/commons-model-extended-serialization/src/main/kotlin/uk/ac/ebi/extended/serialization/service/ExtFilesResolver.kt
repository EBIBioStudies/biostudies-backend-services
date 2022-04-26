package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.io.ext.createTempFile
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.YEAR

class ExtFilesResolver(private val extendFilesPath: File) {

    fun createEmptyFile(subAccNo: String, version: Int, fileName: String): File {
        val name = fileName.replace("/", "-")
        return baseFolder(subAccNo, version).createTempFile(name, ".json")
    }

    private fun baseFolder(subAccNo: String, version: Int): File {
        val now = LocalDate.now()
        val path = extendFilesPath
            .resolve(now.get(YEAR).toString())
            .resolve(now.get(MONTH_OF_YEAR).toString())
            .resolve(now.get(DAY_OF_MONTH).toString())
            .resolve(subAccNo)
            .resolve(version.toString())
        path.mkdirs()
        return path
    }
}
