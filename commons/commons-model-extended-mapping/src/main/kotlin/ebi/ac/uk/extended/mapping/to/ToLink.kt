package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.model.Link
import uk.ac.ebi.biostd.extended.model.ExtLink

internal const val TO_LINK_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToLinkKt"

fun ExtLink.toLink(): Link = Link(url, attributes.mapTo(mutableListOf()) { it.toAttribute() })
