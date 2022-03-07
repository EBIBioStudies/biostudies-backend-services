package ebi.ac.uk.dsl.excel

import org.apache.poi.xssf.streaming.SXSSFSheet

class ExcelSheet(private val sheet: SXSSFSheet) {
    private val rowIterator = generateSequence(0) { it + 1 }.iterator()

    fun row(cellsBuilder: ExcelRow.() -> Unit) = ExcelRow(newRow()).cellsBuilder()

    private fun newRow() = sheet.createRow(rowIterator.next())
}
