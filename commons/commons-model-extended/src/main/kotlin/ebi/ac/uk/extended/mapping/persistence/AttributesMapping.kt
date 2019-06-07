package ebi.ac.uk.extended.mapping.persistence

import ac.uk.ebi.biostd.persistence.model.Attribute
import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail

internal fun toAttributes(attrs: Set<Attribute>) = attrs.map { toAttribute(it) }
internal fun toAttribute(attr: Attribute) = attr.run { ExtAttribute(name, value, reference, toDetails(nameQualifier), toDetails(valueQualifier)) }
internal fun toDetails(details: List<AttributeDetail>) = details.map { ExtAttributeDetail(it.name, it.value) }
