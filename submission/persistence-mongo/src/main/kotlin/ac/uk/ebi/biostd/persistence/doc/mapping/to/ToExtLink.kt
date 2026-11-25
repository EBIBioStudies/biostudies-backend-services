package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import uk.ac.ebi.biostd.base.Either

internal fun DocLink.toExtLink(): ExtLink = ExtLink(url, attributes.toExtAttributes())

internal fun DocLinkTable.toExtLinkTable(): ExtLinkTable = ExtLinkTable(links.map { it.toExtLink() })

internal fun Either<DocLink, DocLinkTable>.toExtLinks(): Either<ExtLink, ExtLinkTable> = bimap({ it.toExtLink() }, { it.toExtLinkTable() })
