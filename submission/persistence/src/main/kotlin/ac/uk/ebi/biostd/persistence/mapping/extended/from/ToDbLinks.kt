package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.Link
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import java.util.SortedSet

fun List<Either<ExtLink, ExtLinkTable>>.toDbLinks(): SortedSet<Link> {
    var idx = 0
    val links = sortedSetOf<Link>()

    for (either in this) {
        when (either) {
            is Either.Left ->
                links.add(either.a.toDbLink(idx++))
            is Either.Right ->
                either.b.links.forEachIndexed { tableIdx, link -> links.add(link.toDbLink(idx++, tableIdx)) }
        }
    }

    return links
}
