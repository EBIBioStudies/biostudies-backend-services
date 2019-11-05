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

    return elements
}

private fun Row.asString(): String = asIterable().joinToString("\t") { it.valueAsString }

private val Cell.valueAsString: String
    get() = when (cellType) {
        NUMERIC -> numericCellValue.toInt().toString()
        else -> stringCellValue
    }
