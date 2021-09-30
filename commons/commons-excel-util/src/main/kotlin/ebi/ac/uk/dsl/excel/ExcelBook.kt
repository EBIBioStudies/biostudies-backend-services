package ebi.ac.uk.dsl.excel

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.OutputStream

class ExcelBook {
    private val workBook = SXSSFWorkbook()

    fun sheet(sheetName: String, sheet: ExcelSheet.() -> Unit) = ExcelSheet(workBook.createSheet(sheetName)).sheet()

    fun write(out: OutputStream) = workBook.write(out)
}
