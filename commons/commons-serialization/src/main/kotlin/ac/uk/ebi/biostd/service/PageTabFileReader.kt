package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.exception.EmptyPageTabFileException
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.file.ExcelReader.readContentAsTsv
import java.io.File

object PageTabFileReader {
    fun readAsPageTab(file: File): File {
        require(file.size() > 0) { throw EmptyPageTabFileException(file.name) }
        return if (SubFormat.fromFile(file) == XlsxTsv) readContentAsTsv(file) else file
    }
}
