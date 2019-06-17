package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.Link
import ebi.ac.uk.extended.model.ExtLink

internal fun Link.toExtLink(): ExtLink = ExtLink(url, attributes.map { it.toExtAttribute() })
