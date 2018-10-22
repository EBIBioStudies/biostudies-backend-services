package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.LinksTable
import ac.uk.ebi.biostd.submission.SimpleAttribute
import ac.uk.ebi.biostd.submission.Table
import arrow.core.Either
import ebi.ac.uk.model.IAttribute
import ebi.ac.uk.model.ISimpleAttribute
import ebi.ac.uk.model.NO_TABLE_INDEX

class ToEntityMapper {

    fun toLinks(links: MutableList<Either<Link, LinksTable>>) =
            links.mapIndexed { index, either -> either.fold({ asLinkList(it, index) }, { asLinkList(it, index) }) }.flatten()

    private fun asLinkList(table: LinksTable, tableIndex: Int) =
            table.elements.mapIndexed { index, link -> asLinkList(link, tableIndex + index) }.flatten()

    private fun asLinkList(link: Link, index: Int) =
            listOf(SerializationLink(link.url, asAttributes(link.attributes), index, NO_TABLE_INDEX))

    private fun asAttributes(attributes: MutableList<Attribute>): MutableList<IAttribute> =
            attributes.mapTo(mutableListOf()) {
                SerializationAttribute(it.name, it.value, it.reference, asSimple(it.terms), asSimple(it.terms))
            }

    private fun asSimple(terms: List<SimpleAttribute>): MutableList<ISimpleAttribute> {
        return mutableListOf()
    }

    fun toLinks2(links: MutableList<Either<Link, LinksTable>>) {
        val z = toTabular(links) { link -> SerializationLink(link.url, asAttributes(link.attributes), 1, NO_TABLE_INDEX) }
    }

    private fun <T : Any, X, W : Table<T>> toTabular(links: List<Either<T, W>>, asTabular: (T) -> X): List<X> {
        return links.mapIndexed { index, either ->
            either.fold({ mapElement(it, index, asTabular) }, { mapTable(it, index, asTabular) })
        }.flatten()
    }

    private fun <T : Any, X, W : Table<T>> mapTable(table: W, tableIndex: Int, asTabular: (T) -> X) =
            table.elements.mapIndexed { index, link -> mapElement(link, tableIndex + index, asTabular) }.flatten()

    private fun <T, X> mapElement(link: T, index: Int, asTabular: (T) -> X) = listOf(asTabular(link))
}

private class TabularMapper {


    private fun <T : Any, X, W : Table<T>> toTabular(elements: List<Either<T, W>>, asTabular: (T) -> X): List<X> {
        return elements.mapIndexed { index, either ->
            either.fold({ mapElement(it, index, asTabular) }, { mapTable(it, index, asTabular) })
        }.flatten()
    }

    private fun <T : Any, X, W : Table<T>> mapTable(table: W, tableIndex: Int, asTabular: (T) -> X) =
            table.elements.mapIndexed { index, link -> mapElement(link, tableIndex + index, asTabular) }.flatten()

    private fun <T, X> mapElement(link: T, index: Int, asTabular: (T) -> X) = listOf(asTabular(link))
}