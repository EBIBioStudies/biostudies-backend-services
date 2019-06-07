package ac.uk.ebi.biostd.persistence.mapping.ext

import ac.uk.ebi.biostd.persistence.model.Link
import ac.uk.ebi.biostd.persistence.model.ext.isTableElement
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.util.collections.component1
import ebi.ac.uk.util.collections.component2

internal fun toExtLinkList(links: Iterable<Link>) = links
    .groupBy { it.isTableElement() }
    .mapValues { it.value.map(::toExtLink) }
    .let { (tableLinks, links) -> links.map { Either.left(it) }.plus(Either.right(ExtLinkTable(tableLinks))) }

internal fun toExtLink(link: Link): ExtLink = ExtLink(link.url, toAttributes(link.attributes))
