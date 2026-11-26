package uk.ac.ebi.biostd.extended.mapping.from

import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtAttributeDetail
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.AttributeDetail

internal const val TO_EXT_ATTRIBUTE_EXTENSIONS = "uk.ac.ebi.biostd.extended.mapping.from.ToExtAttributeKt"

fun Attribute.toExtAttribute(): ExtAttribute = ExtAttribute(name, value, reference, toDetails(nameAttrs), toDetails(valueAttrs))

fun List<Attribute>.toExtAttributes(filterList: Set<String> = emptySet()): List<ExtAttribute> =
    filterNot { filterList.contains(it.name) }.map { it.toExtAttribute() }

private fun toDetails(details: List<AttributeDetail>) = details.map { ExtAttributeDetail(it.name, it.value) }
