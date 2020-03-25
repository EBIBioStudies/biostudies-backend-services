package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbLink
import ebi.ac.uk.extended.model.ExtLink

internal const val TO_EXT_LINK_EXTENSIONS = "ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtLinkKt"

internal fun DbLink.toExtLink(): ExtLink = ExtLink(url, attributes.map { it.toExtAttribute() })
