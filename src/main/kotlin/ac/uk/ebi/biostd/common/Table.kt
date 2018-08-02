package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.serialization.tsv.FILE_TABLE_ID_HADER
import ac.uk.ebi.biostd.serialization.tsv.LINK_TABLE_ID_HEADER
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Section

sealed class Table<T : TableElement>(private val idHeaderName: String) {
    private val headers: MutableSet<TableHeader> = mutableSetOf()
    private val elements: MutableList<TableElement> = mutableListOf()

    fun getHeaders(): List<TableHeader> {
        return listOf(TableHeader(idHeaderName)) + headers.toList()
    }

    fun getRows(): List<TableRow> {
        return elements.map { element ->
            TableRow(element.getId(), element.getIdPropertyName(),
                    headers.map { header -> findAttrByName(header.name, element.getAttributes()) })
        }
    }

    private fun findAttrByName(name: String, attrs: List<Attribute>) =
            attrs.firstOrNull { it.name == name } ?: Attribute.EMPTY

    fun addRow(element: T) {
        headers.addAll(element.getAttributes().map { TableHeader(it.name, it.terms.map { it.first }) })
        elements.add(element)
    }
}

data class TableHeader(val name: String, val termNames: List<String> = listOf()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return name == (other as TableHeader).name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

data class TableRow(val id: String, val idPropertyName: String, val values: List<Attribute>) {
    fun valueList(): List<String> = listOf(id) +
            values.flatMap { attr -> listOf(attr.value) + attr.terms.map { it.second } }
}

class LinksTable : Table<Link>(LINK_TABLE_ID_HEADER)
class SectionsTable(type: String, parentAccNo: String) : Table<Section>("[$type][$parentAccNo]")
class FilesTable : Table<File>(FILE_TABLE_ID_HADER)

interface TableElement {
    fun getAttributes(): List<Attribute>
    fun getId(): String
    fun getIdPropertyName(): String
}
