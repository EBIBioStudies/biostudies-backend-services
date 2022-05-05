package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.EmptyPageTabFileException
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.file.ExcelReader.asTsv
import java.io.File

object PageTabFileReader {
    fun readAsPageTab(file: File): File {
        require(file.size() > 0) { throw EmptyPageTabFileException(file.name) }
        return if (file.extension == "xlsx") asTsv(file) else file
    }
}
