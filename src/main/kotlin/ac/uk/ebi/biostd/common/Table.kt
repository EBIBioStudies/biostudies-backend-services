package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.serialization.tsv.FILE_TABLE_ID_HADER
import ac.uk.ebi.biostd.serialization.tsv.LINK_TABLE_ID_HEADER
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Section

const val NO_VALUE: String = ""

typealias TableRow = List<String>

sealed class Table<T : TableElement>(private val idHeaderName: String) {
    private val headers: LinkedHashSet<String> = linkedSetOf(idHeaderName)
    private val rows: MutableList<Map<String, String>> = mutableListOf()

    fun getHeaders(): List<String> {
        return headers.toList()
    }

    fun getRows(): List<TableRow> {
        return rows.map { headers.map { header -> it.getOrDefault(header, NO_VALUE) } }
    }

    fun addRow(element: T) {
        headers.addAll(element.getAttributes().map { it.name })
        rows.add((mapOf(idHeaderName to element.getId()) + element.getAttributes().map { it.name to it.value }))
    }
}

class LinksTable : Table<Link>(LINK_TABLE_ID_HEADER)
class SectionsTable(type: String, parentAccNo: String) : Table<Section>("[$type][$parentAccNo]")
class FilesTable : Table<File>(FILE_TABLE_ID_HADER)

interface TableElement {
    fun getAttributes(): List<Attribute>
    fun getId(): String
}
