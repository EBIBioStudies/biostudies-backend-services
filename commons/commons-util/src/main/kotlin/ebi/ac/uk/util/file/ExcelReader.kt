package ebi.ac.uk.util.file

import ebi.ac.uk.dsl.Tsv
import ebi.ac.uk.dsl.TsvLine
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class ExcelReader {
    fun readContentAsTsv(file: File): String {
        val sheet = XSSFWorkbook(file.inputStream()).getSheetAt(0)
        val tsvLines = sheet.map(
            { TsvLine(it.map { cell -> cell.valueAsString }) },
            { TsvLine() })

        return Tsv(tsvLines).toString()
    }
}
