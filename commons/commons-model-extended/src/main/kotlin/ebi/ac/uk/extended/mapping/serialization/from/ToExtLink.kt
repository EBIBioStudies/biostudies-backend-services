package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.model.Link

internal const val TO_EXT_LINK_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.from.ToExtLinkKt"

fun Link.toExtLink(): ExtLink = ExtLink(url, attributes.map { it.toExtAttribute() })
