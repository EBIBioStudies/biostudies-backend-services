package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.model.Link

fun ExtLink.toLink(): Link = Link(url, attributes.map { it.toAttribute() })
