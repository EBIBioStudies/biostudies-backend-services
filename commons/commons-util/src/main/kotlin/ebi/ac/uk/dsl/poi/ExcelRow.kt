package ebi.ac.uk.dsl.poi

import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFRow

class ExcelRow(private val row: SXSSFRow) {
    private val cellSequence = buildIterator()
    private fun buildCell() = row.createCell(cellSequence.next())

    fun cell(text: String, range: Int = 1) {
        with(buildCell()) {
            setCellValue(text)
            if (range > 1) {
                sheet.addMergedRegion(CellRangeAddress(rowIndex, rowIndex, columnIndex, columnIndex + range - 1))
                for (i in 1 until range) {
                    buildCell()
                }
            }
        }
    }
}

fun buildIterator(): Iterator<Int> {
    return sequence {
        var value = 0
        while (true) {
            yield(value++)
        }
    }.iterator()
}
