package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable

// Links Mapping
internal fun Either<ExtLink, ExtLinkTable>.toDocLinks() = bimap(ExtLink::toDocLink, ExtLinkTable::toDocLinkTable)

private fun ExtLink.toDocLink(): DocLink = DocLink(url, attributes.map { it.toDocAttribute() })

private fun ExtLinkTable.toDocLinkTable() = DocLinkTable(links.map { it.toDocLink() })

// Attributes Mapping
internal fun ExtAttribute.toDocAttribute() = DocAttribute(name, value, reference, nameAttrs.toExtAttr(), valueAttrs.toExtAttr())

private fun List<ExtAttributeDetail>.toExtAttr() = map { DocAttributeDetail(it.name, it.value) }
