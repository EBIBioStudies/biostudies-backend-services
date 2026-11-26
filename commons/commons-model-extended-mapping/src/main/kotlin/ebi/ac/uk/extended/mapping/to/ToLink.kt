package ebi.ac.uk.extended.mapping.to

import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.model.Link

internal const val TO_LINK_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToLinkKt"

fun ExtLink.toLink(): Link = Link(url, attributes.mapTo(mutableListOf()) { it.toAttribute() })
