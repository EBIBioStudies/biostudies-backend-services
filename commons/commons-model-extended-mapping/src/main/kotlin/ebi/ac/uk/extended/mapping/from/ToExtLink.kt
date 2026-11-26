package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.model.Link
import uk.ac.ebi.biostd.extended.model.ExtLink

internal const val TO_EXT_LINK_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtLinkKt"

fun Link.toExtLink(): ExtLink = ExtLink(url, attributes.map { it.toExtAttribute() })
