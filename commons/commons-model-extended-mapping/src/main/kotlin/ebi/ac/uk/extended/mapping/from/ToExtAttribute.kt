package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

internal const val TO_EXT_ATTRIBUTE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtAttributeKt"

fun Attribute.toExtAttribute(): ExtAttribute = ExtAttribute(name, value, reference, toDetails(nameAttrs), toDetails(valueAttrs))

fun List<Attribute>.toExtAttributes(filterList: Set<String> = emptySet()): List<ExtAttribute> =
    filterNot { filterList.contains(it.name) }.map { it.toExtAttribute() }

private fun toDetails(details: List<AttributeDetail>) = details.map { ExtAttributeDetail(it.name, it.value) }
