package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.DbFile
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import java.util.SortedSet

fun List<Either<ExtFile, ExtFileTable>>.toDbFiles(): SortedSet<DbFile> {
    var idx = 0
    val files = sortedSetOf<DbFile>()

    forEach { either ->
        either.fold(
            { files.add(it.toDbFile(idx++)) },
            { it.files.forEachIndexed { tIdx, sec -> files.add(sec.toDbFile(idx++, tIdx)) } }
        )
    }

    return files
}
