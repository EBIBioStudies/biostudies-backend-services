package ebi.ac.uk.util.file

import ebi.ac.uk.dsl.Tsv
import ebi.ac.uk.dsl.TsvLine
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class ExcelReader {
    fun readContentAsTsv(file: File): String {
        val sheet = XSSFWorkbook(file.inputStream()).getSheetAt(0)
        val tsvLines = sheet.map(
            { TsvLine( it.map { cell -> cell.stringCellValue } ) },
            { TsvLine() })

        return Tsv(tsvLines).toString()
    }
}

// TODO move extensions to separated file
// TODO add docs
// TODO fix problem for numeric values
fun <T> XSSFSheet.map(function: (row: XSSFRow) -> T, emptyRowFunction: () -> T): MutableList<T> {
    val elements: MutableList<T> = mutableListOf()

    (0..lastRowNum).forEach {
        val row = getRow(it)
        if (row == null) {
            elements.add(emptyRowFunction())
        } else {
            elements.add(function(row))
        }
    }

    return elements
}

fun <T> XSSFRow.map(function: (cell: Cell) -> T): MutableList<T> {
    val elements: MutableList<T> = mutableListOf()

    iterator().forEach { elements.add(function(it)) }

    return elements
}
