package ac.uk.ebi.biostd.common

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions.assertThat

fun assertAttributes(
    attributes: List<Attribute>,
    expectedAttributes: Array<out Attribute>,
) = expectedAttributes.forEachIndexed { index, expectedAttribute -> assertAttribute(attributes[index], expectedAttribute) }

private fun assertAttribute(
    attribute: Attribute,
    expectedAttribute: Attribute,
) {
    assertThat(attribute.name).isEqualTo(expectedAttribute.name)
    assertThat(attribute.value).isEqualTo(expectedAttribute.value)
    assertThat(attribute.reference).isEqualTo(expectedAttribute.reference)
    assertAttributeDetails(attribute.nameAttrs, expectedAttribute.nameAttrs)
    assertAttributeDetails(attribute.valueAttrs, expectedAttribute.valueAttrs)
}

private fun assertAttributeDetails(
    detailedAttributes: MutableList<AttributeDetail>,
    expectedDetailedAttributes: MutableList<AttributeDetail>,
) {
    expectedDetailedAttributes.forEachIndexed { index, expectedDetailedAttribute ->
        assertThat(detailedAttributes[index].name).isEqualTo(expectedDetailedAttribute.name)
        assertThat(detailedAttributes[index].value).isEqualTo(expectedDetailedAttribute.value)
    }
}
