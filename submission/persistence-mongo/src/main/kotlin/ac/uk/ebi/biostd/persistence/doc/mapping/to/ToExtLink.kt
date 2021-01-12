package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable

internal fun DocLink.toExtLink(): ExtLink = ExtLink(url, attributes.map { it.toExtAttribute() })

internal fun DocLinkTable.toExtLinkTable(): ExtLinkTable = ExtLinkTable(links.map { it.toExtLink() })

internal fun Either<DocLink, DocLinkTable>.toExtLinks(): Either<ExtLink, ExtLinkTable> =
    bimap({ it.toExtLink() }, { it.toExtLinkTable() })
