package ebi.ac.uk.model

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.model.constants.TableFields
import ebi.ac.uk.model.extensions.parentAccNo
import ebi.ac.uk.util.collections.ifNotEmpty
import java.util.Objects

sealed class Table<T : Any>(elements: List<T>) {

    protected abstract val header: String
    abstract fun toTableRow(t: T): Row<T>

    private val _headers: MutableSet<Header> = mutableSetOf()
    private val _rows: MutableList<Row<T>> = mutableListOf()

    init {
        elements.forEach { addRow(it) }
    }

    val headers: List<Header>
        get() = listOf(Header(header)) + _headers.toList()

    val rows: List<List<String>>
        get() = _rows.mapTo(mutableListOf()) { row -> listOf(row.id) + row.values(_headers.toList()) }

    val elements: List<T>
        get() = _rows.map { it.original }

    fun addRow(data: T) {
        val row = toTableRow(data)
        _headers.addAll(row.headers())
        _rows.add(row)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Table<*>) return false
        if (this === other) return true
        if (header != other.header) return false
        if (_headers != other._headers) return false
        if (_rows != other._rows) return false
        return true
    }

    override fun hashCode() = Objects.hash(header, _headers, _rows)
}

class Header(val name: String, val termNames: List<String> = listOf()) {

    override fun equals(other: Any?): Boolean {
        if (other !is Header) return false
        if (this === other) return true
        return name == other.name
    }

    override fun hashCode() = Objects.hash(name)
}

abstract class Row<T>(val original: T) {
    abstract val id: String
    abstract val attributes: List<Attribute>

    override fun equals(other: Any?): Boolean {
        if (other !is Row<*>) return false
        if (this === other) return true
        return original == other.original
    }

    override fun hashCode() = Objects.hash(original)

    fun headers() = attributes.map { Header(it.name, it.nameAttrs.map(AttributeDetail::name)) }

    fun values(headers: List<Header>) =
        headers.map { findAttrByName(it.name) }
            .flatMap { listOf(it.value) + it.valueAttrs.map(AttributeDetail::value) }

    private fun findAttrByName(name: String) = this.attributes.firstOrNull { it.name == name } ?: Attribute.EMPTY_ATTR
}

class LinksTable(links: List<Link> = emptyList()) : Table<Link>(links) {
    override val header = TableFields.LINKS_TABLE.toString()

    override fun toTableRow(t: Link) = object : Row<Link>(t) {
        override val id = t.url
        override val attributes = t.attributes
    }
}

class FilesTable(files: List<File> = emptyList()) : Table<File>(files) {
    override val header = TableFields.FILES_TABLE.toString()

    override fun toTableRow(t: File) = object : Row<File>(t) {
        override val id = t.path
        override val attributes = t.attributes
    }
}

class SectionsTable(sections: List<Section> = emptyList()) : Table<Section>(sections) {

    private var sectionType = ""
    private var parentAccNo: String? = null

    override val header: String
        get() {
            sectionType.ifBlank { elements.ifNotEmpty {
                elements.first().let {
                    sectionType = it.type
                    parentAccNo = it.parentAccNo
                }
            }}

            return "$sectionType[${if (parentAccNo.isNotBlank()) "$parentAccNo" else ""}]"
        }

    override fun toTableRow(t: Section) = object : Row<Section>(t) {
        override val id = t.accNo!!
        override val attributes = t.attributes
    }
}
