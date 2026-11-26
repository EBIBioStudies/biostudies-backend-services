package uk.ac.ebi.biostd.extended.mapping.from

import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.model.Link

internal const val TO_EXT_LINK_EXTENSIONS = "uk.ac.ebi.biostd.extended.mapping.from.ToExtLinkKt"

fun Link.toExtLink(): ExtLink = ExtLink(url, attributes.map { it.toExtAttribute() })
