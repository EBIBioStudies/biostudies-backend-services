package ebi.ac.uk.dsl.excel

import org.apache.poi.xssf.streaming.SXSSFSheet

class ExcelSheet(private val sheet: SXSSFSheet) {
    private val rowIterator = buildIterator()

    fun row(cellsBuilder: ExcelRow.() -> Unit) = ExcelRow(newRow()).apply { cellsBuilder() }

    fun emptyRow() = row { }

    private fun newRow() = sheet.createRow(rowIterator.next())
}
