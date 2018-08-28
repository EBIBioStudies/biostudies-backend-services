package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.serialization.tsv.FILE_TABLE_ID_HEADER
import ac.uk.ebi.biostd.serialization.tsv.LINK_TABLE_ID_HEADER
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Section
import ac.uk.ebi.biostd.submission.names
import ac.uk.ebi.biostd.submission.values

abstract class Table<T>(elements: Collection<T> = listOf()) {
    abstract val idHeaderName: String
    abstract fun toTableRow(t: T): TableRow<T>

    private val _headers: MutableSet<TableHeader> = mutableSetOf()
    private val _rows: MutableList<TableRow<T>> = elements.map { toTableRow(it) }.toMutableList()

    val headers: List<TableHeader>
        get() = listOf(TableHeader(idHeaderName)) + _headers.toList()

    val rows: Sequence<List<String>>
        get() = _rows.asSequence().map { row ->
            listOf(row.id) + row.values(_headers.toList())
        }

    val elements: List<T>
        get() = _rows.map { it.original }

    fun addRow(data: T) {
        val row = toTableRow(data)
        _headers.addAll(row.headers())
        _rows.add(row)
    }
}

data class TableHeader(val name: String, val termNames: List<String> = listOf()) {
    override fun equals(other: Any?): Boolean {
        other as? TableHeader ?: return false
        if (this === other) return true
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

abstract class TableRow<T>(val original: T) {
    abstract val id: String
    abstract val attributes: List<Attribute>

    fun headers(): List<TableHeader> = attributes.map { TableHeader(it.name, it.terms.names()) }

    fun values(headers: List<TableHeader>): List<String> =
            headers.map { header -> findAttrByName(header.name) }
                    .flatMap { attr -> listOf(attr.value) + attr.terms.values() }

    private fun findAttrByName(name: String) = this.attributes.firstOrNull { it.name == name } ?: Attribute.EMPTY
}

class LinksTable(links: Collection<Link> = emptyList()) : Table<Link>(links) {
    override val idHeaderName: String
        get() = LINK_TABLE_ID_HEADER

    override fun toTableRow(t: Link): TableRow<Link> = object : TableRow<Link>(t) {
        override val id: String
            get() = t.url
        override val attributes: List<Attribute>
            get() = t.attributes
    }
}

class FilesTable(files: Collection<File> = emptyList()) : Table<File>(files) {
    override val idHeaderName: String
        get() = FILE_TABLE_ID_HEADER

    override fun toTableRow(t: File): TableRow<File> = object : TableRow<File>(t) {
        override val id: String
            get() = t.name
        override val attributes: List<Attribute>
            get() = t.attributes
    }
}

class SectionsTable(sections: Collection<Section> = emptyList(), var parentAccNo: String = "") : Table<Section>(sections) {
    private val sectionType: String
        get() = elements.map { it.type }.firstOrNull() ?: ""

    override val idHeaderName: String
        get() = "$sectionType${if (parentAccNo.isEmpty()) "[$parentAccNo]" else ""}"

    override fun toTableRow(t: Section): TableRow<Section> = object : TableRow<Section>(t) {
        override val id: String
            get() = t.accNo
        override val attributes: List<Attribute>
            get() = t.attributes
    }
}
