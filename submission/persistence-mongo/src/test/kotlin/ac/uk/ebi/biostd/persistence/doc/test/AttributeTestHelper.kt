package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import org.assertj.core.api.Assertions.assertThat

private const val ATTR_NAME = "Test Attribute"
private const val ATTR_VALUE = "Test Value"
private const val NAME_ATTR_NAME = "Test Name Attribute"
private const val NAME_ATTR_VALUE = "Test Name Value"
private const val VALUE_ATTR_NAME = "Test Value Attribute"
private const val VALUE_ATTR_VALUE = "Test Value Value"

internal object AttributeTestHelper {
    val basicDocAttribute: DocAttribute = DocAttribute(ATTR_NAME, ATTR_VALUE)
    val fullDocAttribute: DocAttribute =
        DocAttribute(
            name = ATTR_NAME,
            value = ATTR_VALUE,
            nameAttrs = listOf(DocAttributeDetail(NAME_ATTR_NAME, NAME_ATTR_VALUE)),
            valueAttrs = listOf(DocAttributeDetail(VALUE_ATTR_NAME, VALUE_ATTR_VALUE)),
        )

    fun assertBasicExtAttribute(extAttribute: ExtAttribute) {
        assertThat(extAttribute.name).isEqualTo(ATTR_NAME)
        assertThat(extAttribute.value).isEqualTo(ATTR_VALUE)
        assertThat(extAttribute.reference).isFalse
    }

    fun assertFullExtAttribute(extAttribute: ExtAttribute) {
        assertBasicExtAttribute(extAttribute)
        assertExtNameAttributes(extAttribute.nameAttrs)
        assertExtValueAttributes(extAttribute.valueAttrs)
    }

    private fun assertExtNameAttributes(nameAttributes: List<ExtAttributeDetail>) {
        assertThat(nameAttributes).hasSize(1)
        assertExtAttributeDetail(nameAttributes.first(), NAME_ATTR_NAME, NAME_ATTR_VALUE)
    }

    private fun assertExtValueAttributes(valueAttributes: List<ExtAttributeDetail>) {
        assertThat(valueAttributes).hasSize(1)
        assertExtAttributeDetail(valueAttributes.first(), VALUE_ATTR_NAME, VALUE_ATTR_VALUE)
    }

    private fun assertExtAttributeDetail(
        extAttribute: ExtAttributeDetail,
        name: String,
        value: String,
    ) {
        assertThat(extAttribute.name).isEqualTo(name)
        assertThat(extAttribute.value).isEqualTo(value)
    }
}
