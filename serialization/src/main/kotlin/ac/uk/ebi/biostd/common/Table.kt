package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.serialization.tsv.FILE_TABLE_ID_HEADER
import ac.uk.ebi.biostd.serialization.tsv.LINK_TABLE_ID_HEADER
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Section
import ac.uk.ebi.biostd.submission.names
import ac.uk.ebi.biostd.submission.values
import ebi.ac.uk.base.EMPTY

abstract class Table<T>(elements: Collection<T>) {
    abstract val idHeaderName: String
    abstract fun toTableRow(t: T): TableRow<T>

    private val _headers: MutableSet<TableHeader> = mutableSetOf()
    private val _rows: MutableList<TableRow<T>> = mutableListOf()

    init {
        elements.forEach { addRow(it) }
    }

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

    override fun equals(other: Any?): Boolean {
        other as? Table<*> ?: return false
        if (this === other) return true

        if (idHeaderName != other.idHeaderName) return false
        if (_headers != other._headers) return false
        if (_rows != other._rows) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idHeaderName.hashCode()
        result = 31 * result + _headers.hashCode()
        result = 31 * result + _rows.hashCode()
        return result
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
                    .flatMap { listOf(it.value) + it.terms.values() }

    private fun findAttrByName(name: String) = this.attributes.firstOrNull { it.name == name } ?: Attribute.EMPTY_ATTR

    override fun equals(other: Any?): Boolean {
        other as? TableRow<*> ?: return false
        if (this === other) return true
        return original == other.original
    }

    override fun hashCode(): Int {
        return original?.hashCode() ?: 0
    }
}

class LinksTable(links: List<Link> = emptyList()) : Table<Link>(links) {
    override val idHeaderName = LINK_TABLE_ID_HEADER

    override fun toTableRow(t: Link): TableRow<Link> = object : TableRow<Link>(t) {
        override val id = t.url
        override val attributes = t.attributes
    }
}

class FilesTable(files: List<File> = emptyList()) : Table<File>(files) {
    override val idHeaderName = FILE_TABLE_ID_HEADER

    override fun toTableRow(t: File): TableRow<File> = object : TableRow<File>(t) {
        override val id = t.name
        override val attributes = t.attributes
    }
}

class SectionsTable(sections: List<Section> = emptyList(), var parentAccNo: String = EMPTY) : Table<Section>(sections) {
    private val sectionType = elements.map { it.type }.firstOrNull() ?: EMPTY

    override val idHeaderName = "$sectionType${if (parentAccNo.isEmpty()) "[$parentAccNo]" else EMPTY}"

    override fun toTableRow(t: Section): TableRow<Section> = object : TableRow<Section>(t) {
        override val id = t.accNo!!
        override val attributes = t.attributes
    }
}
