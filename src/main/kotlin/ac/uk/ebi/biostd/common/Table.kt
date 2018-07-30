package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.submission.Attribute

const val NO_VALUE: String = ""

typealias TableRow = List<String>

data class Table<T : TableElement>(val idHeaderName: String) {
    private val headers: LinkedHashSet<String> = linkedSetOf(idHeaderName)
    private val rows: MutableList<Map<String, String>> = mutableListOf()

    fun getHeaders(): List<String> {
        return headers.toList()
    }

    fun getRows(): List<TableRow> {
        return rows.map {
            headers.map { header -> it.getOrDefault(header, NO_VALUE) }
        }
    }

    fun addRow(element: T) {
        headers.addAll(element.attributes.map { it.name })

        rows.add(
                (mapOf(idHeaderName to element.id) +
                        element.attributes.map { it.name to it.value }))
    }
}

interface TableElement {
    val id: String
    val attributes: List<Attribute>
}

