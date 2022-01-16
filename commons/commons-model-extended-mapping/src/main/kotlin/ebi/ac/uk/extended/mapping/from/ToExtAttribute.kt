package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.constants.SectionFields

internal const val TO_EXT_ATTRIBUTE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtAttributeKt"

// TODO: remove section attribute this does not bellow to this abtraction level.
fun Attribute.toExtAttribute(): ExtAttribute {
    val attrValue = if (name == SectionFields.FILE_LIST.value) value?.substringBeforeLast(".") else value
    return ExtAttribute(name, attrValue, reference, toDetails(nameAttrs), toDetails(valueAttrs))
}

fun List<Attribute>.toExtAttributes(): List<ExtAttribute> = map { it.toExtAttribute() }

private fun toDetails(details: List<AttributeDetail>) = details.map { ExtAttributeDetail(it.name, it.value) }
