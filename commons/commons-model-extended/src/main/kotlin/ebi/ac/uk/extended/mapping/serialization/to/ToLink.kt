package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.model.Link

fun ExtLink.toLink(): Link = Link(url, attributes.mapTo(mutableListOf()) { it.toAttribute() })

internal const val TO_LINK_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.to.ToLinkKt"
