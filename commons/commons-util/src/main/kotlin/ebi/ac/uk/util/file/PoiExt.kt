package ebi.ac.uk.util.file

import ebi.ac.uk.base.EMPTY
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

/**
 * Retrieves the sheet representation as a list of tsv separated string List. Note that as stream reader ignored empty
 * rows they are completed with empty values.
 */
fun Sheet.asTsvSequence(): Sequence<String> {
    val sheet = this
    return sequence {
        var elements = 0
        for (row in sheet) {
            while (elements < row.rowNum) {
                elements++
                yield(EMPTY)
            }

            elements++
            yield(row.asString())
        }
    }
}

private val specialCharRegex = "[\n\t\"]".toRegex()

private fun Row.asString(): String {
    val cells = mutableListOf<String>()
    for (idx in 0 until lastCellNum) {
        cells.add(getCell(idx)?.valueAsString ?: EMPTY)
    }

    return when {
        cells.all { it == EMPTY } -> EMPTY
        else ->
            cells
                .dropLastWhile { it.isBlank() }
                .joinToString("\t") { if (it.contains(specialCharRegex)) "\"${it}\"" else it }
    }
}

private val Cell.valueAsString: String
    get() = when (cellType) {
        NUMERIC -> numericCellValue.toInt().toString()
        else -> stringCellValue
    }
