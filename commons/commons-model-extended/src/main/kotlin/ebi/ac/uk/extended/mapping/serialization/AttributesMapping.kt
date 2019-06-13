package ebi.ac.uk.extended.mapping.serialization

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

class AttributeMapper {

    internal fun toAttributes(attrs: List<Attribute>) = attrs.map { toAttribute(it) }
    private fun toAttribute(attr: Attribute) = attr.run { ExtAttribute(name, value, reference, toDetails(nameAttrs), toDetails(valueAttrs)) }
    private fun toDetails(details: List<AttributeDetail>) = details.map { ExtAttributeDetail(it.name, it.value) }
}
