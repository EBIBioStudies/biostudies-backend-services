package ebi.ac.uk.model

import ebi.ac.uk.model.extensions.nameAttrsNames
import ebi.ac.uk.model.extensions.nameAttrsValues
import ebi.ac.uk.model.extensions.valueAttrsNames
import ebi.ac.uk.model.extensions.valueAttrsValues
import java.util.Objects

sealed class Table<T : Any>(elements: List<T>) {
    abstract fun toTableRow(t: T): Row<T>

    val headers: MutableSet<Header> = mutableSetOf()
    private val _rows: MutableList<Row<T>> = mutableListOf()

    init {
        elements.forEach { addRow(it) }
    }

    val rows: List<List<String>>
        get() = _rows.mapTo(mutableListOf()) { row -> listOf(row.id) + row.values(headers.toList()) }

    val elements: List<T>
        get() = _rows.map { it.original }

    fun addRow(data: T) {
        val row = toTableRow(data)
        headers.addAll(row.headers())
        _rows.add(row)
    }

    override fun equals(other: Any?) = when {
        other !is Table<*> -> false
        other === this -> true
        else -> Objects.equals(headers, other.headers)
            .and(Objects.equals(_rows, other._rows))
    }

    override fun hashCode() = Objects.hash(headers, _rows)
}

class Header(val name: String, val termNames: List<String> = listOf(), val termValues: List<String> = listOf()) {

    override fun equals(other: Any?) = when {
        other !is Header -> false
        this === other -> true
        else -> Objects.equals(name, other.name)
    }

    override fun hashCode() = Objects.hash(name)
}

abstract class Row<T>(val original: T) {
    abstract val id: String
    abstract val attributes: List<Attribute>

    override fun equals(other: Any?) = when {
        other !is Row<*> -> false
        this === other -> true
        else -> original == other.original
    }

    override fun hashCode() = Objects.hash(original)

    fun headers() = attributes.map { Header(it.name, it.nameAttrsNames, it.valueAttrsNames) }

    fun values(headers: List<Header>) =
        headers
            .map { findAttrByName(it.name) }
            .flatMap { listOf(it.value) + it.nameAttrsValues + it.valueAttrsValues }

    private fun findAttrByName(name: String) = this.attributes.firstOrNull { it.name == name } ?: Attribute.EMPTY_ATTR
}

class LinksTable(links: List<Link> = emptyList()) : Table<Link>(links) {
    override fun toTableRow(t: Link) = object : Row<Link>(t) {
        override val id = t.url
        override val attributes = t.attributes
    }
}

class FilesTable(files: List<File> = emptyList()) : Table<File>(files) {
    override fun toTableRow(t: File) = object : Row<File>(t) {
        override val id = t.path
        override val attributes = t.attributes
    }
}

class SectionsTable(sections: List<Section> = emptyList()) : Table<Section>(sections) {
    override fun toTableRow(t: Section) = object : Row<Section>(t) {
        override val id = t.accNo!!
        override val attributes = t.attributes
    }

    fun asSectionsTable() = SectionsTable(
        elements.map { if (it is ExtendedSection) it.asSection() else it })
}
