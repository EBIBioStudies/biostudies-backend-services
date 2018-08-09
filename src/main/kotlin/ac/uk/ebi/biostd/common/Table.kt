package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.serialization.tsv.FILE_TABLE_ID_HADER
import ac.uk.ebi.biostd.serialization.tsv.LINK_TABLE_ID_HEADER
import ac.uk.ebi.biostd.submission.*

sealed class Table<T>(private val idHeaderName: String, val convert: (t: T) -> TableRow) {
    private val headers: MutableSet<TableHeader> = mutableSetOf()
    private val rows: MutableList<TableRow> = mutableListOf()

    fun getHeaders(): List<TableHeader> = listOf(TableHeader(idHeaderName)) + headers.toList()

    fun getRows(): List<TableRow> = rows.toList()

    fun getValues(): Sequence<List<String>> {
        return rows.asSequence().map { row ->
            listOf(row.id) + row.attr(headers.toList()).flatMap { attr -> listOf(attr.value) + attr.terms.values() }
        }
    }

    fun addRow(data: T) {
        val row = this.convert(data)
        headers.addAll(row.attributes.map { TableHeader(it.name, it.terms.names()) })
        rows.add(row)
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

class LinksTable : Table<Link>(LINK_TABLE_ID_HEADER, {
    TableRow(it.url, "url", it.attrs)
})

class FilesTable : Table<File>(FILE_TABLE_ID_HADER, {
    TableRow(it.name, "path", it.attrs)
})

class SectionsTable(type: String, parentAccNo: String) : Table<Section>("[$type][$parentAccNo]", {
    TableRow(it.accNo, "accNo", it.attrs)
})

data class TableRow(val id: String, val idPropertyName: String, val attributes: List<Attribute>) {

    fun attr(headers: List<TableHeader>): List<Attribute> = headers.map { header -> findAttrByName(header.name) }

    private fun findAttrByName(name: String) = this.attributes.firstOrNull { it.name == name } ?: Attribute.EMPTY
}
