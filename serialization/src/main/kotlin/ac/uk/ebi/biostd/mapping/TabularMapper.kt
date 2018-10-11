package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.submission.FilesTable
import ac.uk.ebi.biostd.submission.LinksTable
import arrow.core.Either
import ebi.ac.uk.model.IFile
import ebi.ac.uk.model.ILink
import ebi.ac.uk.model.ITabular
import ebi.ac.uk.model.NO_TABLE_INDEX
import ebi.ac.uk.util.collections.ifNotEmpty

class TabularMapper(private val attributesMapper: AttributesMapper) {

    fun toLinks(links: Set<ILink>) = mapTabular(links, attributesMapper::toLink, ::LinksTable)
    fun toFiles(links: Set<IFile>) = mapTabular(links, attributesMapper::toFile, ::FilesTable)

    private fun <DbType : ITabular, Type, TableType> mapTabular(
            elements: Set<DbType>,
            transform: (DbType) -> Type,
            tableBuilder: (List<Type>) -> TableType): MutableList<Either<Type, TableType>> {

        val (listElements, tableElements) = elements.partition { it.tableIndex == NO_TABLE_INDEX }

        val map: MutableMap<Int, Either<Type, TableType>> = mutableMapOf()
        listElements.forEach { map[it.order] = Either.Left(transform(it)) }
        tableElements.ifNotEmpty { table -> map[min(table)] = Either.Right(tableBuilder(table.map { transform(it) })) }
        return map.toSortedMap().values.toMutableList()
    }

    private fun min(list: Collection<ITabular>) = list.map { it.order }.min()!!
}
