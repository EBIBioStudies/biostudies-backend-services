package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.model.Link

internal const val TO_LINK_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToLinkKt"

fun ExtLink.toLink(): Link = Link(url, attributes.mapTo(mutableListOf()) { it.toAttribute() })
