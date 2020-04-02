package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.DbLink
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import java.util.SortedSet

fun List<Either<ExtLink, ExtLinkTable>>.toDbLinks(): SortedSet<DbLink> {
    var idx = 0
    val links = sortedSetOf<DbLink>()

    forEach { either ->
        either.fold(
            { links.add(it.toDbLink(idx++)) },
            { it.links.forEachIndexed { tIdx, sec -> links.add(sec.toDbLink(idx++, tIdx)) } }
        )
    }

    return links
}
