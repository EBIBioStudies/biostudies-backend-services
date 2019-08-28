package ebi.ac.uk.dsl.poi

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.OutputStream

class ExcelBook {
    private val workBook = SXSSFWorkbook()

    fun sheet(sheetName: String, rowsBuilder: ExcelSheet.() -> Unit) =
        ExcelSheet(workBook.createSheet(sheetName)).apply { rowsBuilder() }

    fun write(out: OutputStream) = workBook.write(out)
}
