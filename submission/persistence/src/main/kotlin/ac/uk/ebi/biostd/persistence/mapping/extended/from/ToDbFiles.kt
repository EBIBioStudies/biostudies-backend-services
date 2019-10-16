package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.File
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import java.util.SortedSet

fun List<Either<ExtFile, ExtFileTable>>.toDbFiles(): SortedSet<File> {
    var idx = 0
    val files = sortedSetOf<File>()

    for (either in this) {
        when (either) {
            is Either.Left ->
                files.add(either.a.toDbFile(idx++))
            is Either.Right ->
                either.b.files.forEachIndexed { tableIdx, file -> files.add(file.toDbFile(idx++, tableIdx)) }
        }
    }

    return files
}
