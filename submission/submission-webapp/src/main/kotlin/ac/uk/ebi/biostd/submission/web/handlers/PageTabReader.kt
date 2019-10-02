package ac.uk.ebi.biostd.submission.web.handlers

import ebi.ac.uk.io.isExcel
import ebi.ac.uk.util.file.ExcelReader
import java.io.File

class PageTabReader(private val excelReader: ExcelReader) {
    fun read(file: File): String =
        if (file.isExcel()) excelReader.readContentAsTsv(file) else file.readText()
}
