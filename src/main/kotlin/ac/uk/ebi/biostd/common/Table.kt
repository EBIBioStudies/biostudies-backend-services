package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.submission.Attribute

data class Table<T : TableElement>(val mainHeaderName: String) {

    fun getHeaders(): List<String> {
        return listOf()
    }

    fun getRows(): List<TableRow> {
        return listOf()
    }

    fun addRow(element: T) {
    }
}

data class TableRow(val values: List<String>)

interface TableElement {
    val id: String
    val attributes: List<Attribute>
}

