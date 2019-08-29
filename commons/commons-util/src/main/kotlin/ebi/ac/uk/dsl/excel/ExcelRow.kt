package ebi.ac.uk.dsl.excel

import org.apache.poi.xssf.streaming.SXSSFRow

class ExcelRow(private val row: SXSSFRow) {
    private val cellIterator = buildIterator()

    fun cell(text: String) {
        newCell().apply { setCellValue(text) }
    }

    private fun newCell() = row.createCell(cellIterator.next())
}

fun buildIterator(): Iterator<Int> = generateSequence(0) { it + 1 }.iterator()
