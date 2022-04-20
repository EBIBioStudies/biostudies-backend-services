package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.io.ext.createNewFile
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.YEAR

class ExtFilesResolver(private val extendFilesPath: File) {

    fun createEmptyFile(subAccNo: String, version: Int, fileName: String): File =
        baseFolder(subAccNo, version).createNewFile(fileName)

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
