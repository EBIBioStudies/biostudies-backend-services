package ebi.ac.uk.util.file

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

/**
 * Maps the sheet's rows, including empty rows, to a list of elements of the specified type applying the given functions
 *
 * @param function The mapping function to be applied over non empty rows.
 * @param emptyRowFunction The mapping function to be applied over empty rows. The current implementation of the rows
 * iterator ignores the blank rows, that's why it's necessary to provide a function to treat these.
 */
fun <T> Sheet.map(function: (row: Row) -> T, emptyRowFunction: () -> T): MutableList<T> {
    val elements: MutableList<T> = mutableListOf()
    rowIterator().forEach {
        if (it == null) {
            elements.add(emptyRowFunction())
        } else {
            elements.add(function(it))
        }
    }

    return elements
}

/**
 * Maps the row's cells to a list of elements of the specified type applying the given function.
 *
 * @param function The mapping function to be applied over the cells.
 */
fun <T> Row.map(function: (cell: Cell) -> T): MutableList<T> {
    val elements: MutableList<T> = mutableListOf()

    iterator().forEach { elements.add(function(it)) }

    return elements
}

/**
 * Cell value as string.
 */
val Cell.valueAsString: String
    get() = when (cellType) {
        NUMERIC -> numericCellValue.toInt().toString()
        else -> stringCellValue
    }
