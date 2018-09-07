package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.serialization.tsv.FILE_TABLE_ID_HEADER
import ac.uk.ebi.biostd.serialization.tsv.LINK_TABLE_ID_HEADER
import ebi.ac.uk.base.EMPTY
import java.util.*

abstract class Table<T>(elements: List<T>) {
    abstract val idHeaderName: String
    abstract fun toTableRow(t: T): Row<T>

    private val _headers: MutableSet<Header> = mutableSetOf()
    private val _rows: MutableList<Row<T>> = mutableListOf()

    init {
        elements.forEach { addRow(it) }
    }

    val headers: List<Header>
        get() = listOf(Header(idHeaderName)) + _headers.toList()

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

    override fun equals(other: Any?): Boolean {
        other as? Table<*> ?: return false
        if (this === other) return true
        if (idHeaderName != other.idHeaderName) return false
        if (_headers != other._headers) return false
        if (_rows != other._rows) return false
        return true
    }

    override fun hashCode() = Objects.hash(idHeaderName, _headers, _rows)
}

data class Header(val name: String, val termNames: List<String> = listOf()) {

    override fun equals(other: Any?): Boolean {
        other as? Header ?: return false
        if (this === other) return true
        return name == other.name
    }

    override fun hashCode() = Objects.hash(name)
}

abstract class Row<T>(val original: T) {
    abstract val id: String
    abstract val attributes: List<Attribute>

    override fun equals(other: Any?): Boolean {
        other as? Row<*> ?: return false
        if (this === other) return true
        return original == other.original
    }

    override fun hashCode() = Objects.hash(original)

    fun headers() = attributes.map { Header(it.name, it.terms.names()) }

    fun values(headers: List<Header>) = headers.map { findAttrByName(it.name) }.flatMap { listOf(it.value) + it.terms.values() }

    private fun findAttrByName(name: String) = this.attributes.firstOrNull { it.name == name } ?: Attribute.EMPTY_ATTR
}

class LinksTable(links: List<Link> = emptyList()) : Table<Link>(links) {
    override val idHeaderName = LINK_TABLE_ID_HEADER

    override fun toTableRow(t: Link) = object : Row<Link>(t) {
        override val id = t.url
        override val attributes = t.attributes
    }
}

class FilesTable(files: List<File> = emptyList()) : Table<File>(files) {
    override val idHeaderName = FILE_TABLE_ID_HEADER

    override fun toTableRow(t: File) = object : Row<File>(t) {
        override val id = t.name
        override val attributes = t.attributes
    }
}

class SectionsTable(sections: List<Section> = emptyList(), var parentAccNo: String = EMPTY) : Table<Section>(sections) {
    private val sectionType = elements.map { it.type }.firstOrNull().orEmpty()

    override val idHeaderName = "$sectionType${if (parentAccNo.isEmpty()) "[$parentAccNo]" else EMPTY}"

    override fun toTableRow(t: Section) = object : Row<Section>(t) {
        override val id = t.accNo!!
        override val attributes = t.attributes
    }
}
