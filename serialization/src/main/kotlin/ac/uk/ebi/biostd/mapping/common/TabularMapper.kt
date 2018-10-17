package ac.uk.ebi.biostd.mapping.common

import arrow.core.Either
import ebi.ac.uk.model.ITabular
import ebi.ac.uk.model.NO_TABLE_INDEX
import ebi.ac.uk.util.collections.ifNotEmpty

class TabularMapper {

    fun <DbType : ITabular, Type, TableType> mapTabular(
            elements: Set<DbType>,
            transform: (DbType) -> Type,
            tableBuilder: (List<Type>) -> TableType): MutableList<Either<Type, TableType>> {

        val (listElements, tableElements) = elements.partition { it.tableIndex == NO_TABLE_INDEX }

        val map: MutableMap<Int, Either<Type, TableType>> = mutableMapOf()
        listElements.forEach { map[it.order] = Either.Left(transform(it)) }
        tableElements.ifNotEmpty { table -> map[min(table)] = Either.Right(tableBuilder(table.map { transform(it) })) }
        return map.toSortedMap().values.toMutableList()
    }

    private fun min(list: List<ITabular>) = list.minBy { it.order }!!.order
}
