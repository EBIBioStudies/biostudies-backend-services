package ebi.ac.uk.extended.mapping.to

import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtAttributeDetail
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.AttributeDetail

internal const val TO_ATTRIBUTE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToAttributeKt"

fun ExtAttribute.toAttribute() = Attribute(name, value, reference, toDetails(nameAttrs), toDetails(valueAttrs))

private fun toDetails(details: List<ExtAttributeDetail>): MutableList<AttributeDetail> =
    details.mapTo(mutableListOf()) { AttributeDetail(it.name, it.value) }
