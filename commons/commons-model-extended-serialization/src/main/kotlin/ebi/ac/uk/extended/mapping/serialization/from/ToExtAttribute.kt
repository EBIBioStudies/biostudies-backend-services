package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.constants.SectionFields

internal const val TO_EXT_ATTRIBUTE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.from.ToExtAttributeKt"

fun Attribute.toExtAttribute(): ExtAttribute {
    val attrValue = if (name == SectionFields.FILE_LIST.value) value.substringBeforeLast(".") else value
    return ExtAttribute(name, attrValue, reference, toDetails(nameAttrs), toDetails(valueAttrs))
}

private fun toDetails(details: List<AttributeDetail>) = details.map { ExtAttributeDetail(it.name, it.value) }
