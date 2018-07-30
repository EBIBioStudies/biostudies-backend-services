package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.submission.Attribute

const val NO_VALUE: String = ""

typealias TableRow = List<String>

data class Table<T : TableElement>(val idHeaderName: String) {
    private val header: LinkedHashSet<String> = linkedSetOf(idHeaderName)
    private val rows: MutableList<Map<String, String>> = mutableListOf()

    fun getHeaders(): List<String> {
        return header.toList()
    }

    fun getRows(): List<TableRow> {
        return rows.map {
            header.map { h -> it.getOrDefault(h, NO_VALUE) }
        }
    }

    fun addRow(element: T) {
        header.addAll(element.attributes.map { it.name })

        rows.add(
                (mapOf(idHeaderName to element.id) +
                        element.attributes.map { it.name to it.value }.toMap()))
    }
}

interface TableElement {
    val id: String
    val attributes: List<Attribute>
}

