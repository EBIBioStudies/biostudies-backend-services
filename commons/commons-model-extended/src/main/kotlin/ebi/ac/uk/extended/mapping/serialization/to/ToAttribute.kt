package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

fun ExtAttribute.toAttribute() = Attribute(name, value, reference, toDetails(nameAttrs), toDetails(valueAttrs))
private fun toDetails(details: List<ExtAttributeDetail>): MutableList<AttributeDetail> =
    details.mapTo(mutableListOf()) { AttributeDetail(it.name, it.value) }

internal const val TO_ATTRIBUTE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.to.ToAttributeKt"
