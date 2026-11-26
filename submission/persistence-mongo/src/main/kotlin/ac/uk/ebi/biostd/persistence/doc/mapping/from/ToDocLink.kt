package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import uk.ac.ebi.biostd.base.Either
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtAttributeDetail
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.extended.model.ExtLinkTable

// Links Mapping
internal fun Either<ExtLink, ExtLinkTable>.toDocLinks() = bimap(ExtLink::toDocLink, ExtLinkTable::toDocLinkTable)

internal fun ExtLink.toDocLink(): DocLink = DocLink(url, attributes.map { it.toDocAttribute() })

private fun ExtLinkTable.toDocLinkTable() = DocLinkTable(links.map { it.toDocLink() })

// Attributes Mapping
internal fun ExtAttribute.toDocAttribute() = DocAttribute(name, value, reference, nameAttrs.toExtAttr(), valueAttrs.toExtAttr())

private fun List<ExtAttributeDetail>.toExtAttr() = map { DocAttributeDetail(it.name, it.value) }
