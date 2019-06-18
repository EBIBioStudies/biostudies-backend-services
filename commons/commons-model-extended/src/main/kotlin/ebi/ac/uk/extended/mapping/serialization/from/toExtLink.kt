package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.model.Link

fun Link.toExtLink(): ExtLink = ExtLink(url, attributes.map { it.toExtAttribute() })
