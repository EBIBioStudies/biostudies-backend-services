package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.common.FilesTable
import ac.uk.ebi.biostd.common.LinksTable
import ac.uk.ebi.biostd.config.FileDb
import ac.uk.ebi.biostd.config.LinkDb
import ac.uk.ebi.biostd.persistence.model.Tabular
import arrow.core.Either
import ebi.ac.uk.util.collections.ifNotEmpty

class TabularMapper(private val attributesMapper: AttributesMapper) {

    fun toLinks(links: Set<LinkDb>) = mapTabular(links, attributesMapper::toLink, ::LinksTable)
    fun toFiles(links: Set<FileDb>) = mapTabular(links, attributesMapper::toFile, ::FilesTable)

    private fun <DbType : Tabular, Type, TableType> mapTabular(
            elements: Set<DbType>,
            transform: (DbType) -> Type,
            tableBuilder: (List<Type>) -> TableType): MutableList<Either<Type, TableType>> {

        val (listElements, tableElements) = elements.partition { it.tableIndex == -1 }

        val map: MutableMap<Int, Either<Type, TableType>> = mutableMapOf()
        listElements.forEach { map[it.order] = Either.Left(transform(it)) }
        tableElements.ifNotEmpty { table -> map[min(table)] = Either.Right(tableBuilder(tableElements.map { transform(it) })) }
        return map.toSortedMap().values.toMutableList()
    }

    private fun min(list: List<Tabular>) = list.map { it.order }.min()!!
}