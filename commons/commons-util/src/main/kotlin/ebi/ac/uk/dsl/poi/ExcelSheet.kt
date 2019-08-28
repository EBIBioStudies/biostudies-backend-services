package ebi.ac.uk.dsl.poi

import org.apache.poi.xssf.streaming.SXSSFSheet

class ExcelSheet(private val sheet: SXSSFSheet) {
    private val rowSequence = buildIterator()

    fun row(height: Short? = null, init: ExcelRow.() -> Unit): ExcelRow {
        val sxssfRow = sheet.createRow(rowSequence.next())
        if (height != null) {
            sxssfRow.height = height
        }

        return ExcelRow(sxssfRow).also { it.init() }
    }

    fun emptyRow(height: Short? = null, repeat: Int = 1) {
        for (i in 1..repeat) {
            row(height) { }
        }
    }
}
