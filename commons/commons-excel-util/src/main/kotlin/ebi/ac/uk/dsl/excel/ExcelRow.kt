package ebi.ac.uk.dsl.excel

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.streaming.SXSSFRow

class ExcelRow(private val row: SXSSFRow) {
    private val cellIterator = generateSequence(0) { it + 1 }.iterator()

    fun cell(text: String) {
        val cell = row.createCell(cellIterator.next(), CellType.STRING)
        cell.setCellValue(text)
    }
}
