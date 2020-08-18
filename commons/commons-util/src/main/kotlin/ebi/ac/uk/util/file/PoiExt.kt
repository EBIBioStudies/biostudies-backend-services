package ebi.ac.uk.util.file

import ebi.ac.uk.base.EMPTY
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import java.util.LinkedList

/**
 * Retrieves the sheet representation as a list of tsv separated string List. Note that as stream reader ignored empty
 * rows they are completed with emtpy values.
 */
fun Sheet.asTsvList(): List<String> {
    val elements = LinkedList<String>()
    for (row in this) {
        while (elements.size < row.rowNum) elements.add(EMPTY)
        elements.add(row.asString())
    }

    return elements.trim()
}

private fun Row.asString(): String {
    val cells = mutableListOf<String>()
    for (idx in 0 until lastCellNum) {
        cells.add(getCell(idx)?.valueAsString ?: EMPTY)
    }

    return when {
        cells.all { it == EMPTY } -> EMPTY
        else -> cells.trim().joinToString("\t")
    }
}

private fun MutableList<String>.trim(): List<String> = when {
    last().isBlank() -> dropLastWhile { it.isBlank() }
    else -> this
}

private val Cell.valueAsString: String
    get() = when (cellType) {
        NUMERIC -> numericCellValue.toInt().toString()
        else -> stringCellValue
    }
