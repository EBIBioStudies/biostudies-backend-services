package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

fun Attribute.toExtAttribute() = ExtAttribute(name, value, reference, toDetails(nameAttrs), toDetails(valueAttrs))
private fun toDetails(details: List<AttributeDetail>) = details.map { ExtAttributeDetail(it.name, it.value) }
