package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions.assertThat

val attribute
    get() =
        Attribute(
            name = "SectAttr",
            value = "Sect Attr 1",
            reference = false,
            nameAttrs = mutableListOf(AttributeDetail("NameAttr", "Name Attr 1")),
            valueAttrs = mutableListOf(AttributeDetail("ValueAttr", "Value Attr 1")),
        )

fun assertAttribute(
    extAttribute: ExtAttribute,
    attribute: Attribute,
) {
    assertThat(extAttribute.name).isEqualTo(attribute.name)
    assertThat(extAttribute.value).isEqualTo(attribute.value)
    assertThat(extAttribute.reference).isEqualTo(attribute.reference)

    val nameAttribute = nameAttribute(attribute)
    val extNameAttribute = extNameAttribute(extAttribute)
    assertThat(extNameAttribute.name).isEqualTo(nameAttribute.name)
    assertThat(extNameAttribute.value).isEqualTo(nameAttribute.value)

    val valueAttribute = valueAttribute(attribute)
    val extValueAttribute = extValueAttribute(extAttribute)
    assertThat(extValueAttribute.name).isEqualTo(valueAttribute.name)
    assertThat(extValueAttribute.value).isEqualTo(valueAttribute.value)
}

private fun nameAttribute(attribute: Attribute): AttributeDetail {
    assertThat(attribute.nameAttrs).hasSize(1)
    return attribute.nameAttrs.first()
}

private fun extNameAttribute(attribute: ExtAttribute): ExtAttributeDetail {
    assertThat(attribute.nameAttrs).hasSize(1)
    return attribute.nameAttrs.first()
}

private fun valueAttribute(attribute: Attribute): AttributeDetail {
    assertThat(attribute.valueAttrs).hasSize(1)
    return attribute.valueAttrs.first()
}

private fun extValueAttribute(attribute: ExtAttribute): ExtAttributeDetail {
    assertThat(attribute.valueAttrs).hasSize(1)
    return attribute.valueAttrs.first()
}
