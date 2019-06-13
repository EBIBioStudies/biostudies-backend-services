package ac.uk.ebi.biostd.persistence.mapping.ext

import ac.uk.ebi.biostd.persistence.model.Attribute
import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail

internal fun toAttributes(attrs: Set<Attribute>) = attrs.map { toAttribute(it) }
internal fun toAttribute(attr: Attribute) =
    ExtAttribute(attr.name, attr.value, attr.reference, toDetails(attr.nameQualifier), toDetails(attr.valueQualifier))
internal fun toDetails(details: List<AttributeDetail>) = details.map { ExtAttributeDetail(it.name, it.value) }
