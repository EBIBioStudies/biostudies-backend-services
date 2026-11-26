package uk.ac.ebi.biostd.extended.mapping.to

import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.model.Link

internal const val TO_LINK_EXTENSIONS = "uk.ac.ebi.biostd.extended.mapping.to.ToLinkKt"

fun ExtLink.toLink(): Link = Link(url, attributes.mapTo(mutableListOf()) { it.toAttribute() })
