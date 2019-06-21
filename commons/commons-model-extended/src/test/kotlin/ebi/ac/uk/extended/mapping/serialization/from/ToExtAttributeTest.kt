package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToExtAttributeTest {

    private val expectedNameAttribute = AttributeDetail(name = "Name Attribute Name", value = "Name Attribute Value")
    private val expectedValueAttribute = AttributeDetail(name = "Value Attribute Name", value = "Value Attribute Value")

    private val attribute = Attribute(
        name = "Attribute Name",
        value = "Attribute Value",
        reference = true,
        nameAttrs = mutableListOf(expectedNameAttribute),
        valueAttrs = mutableListOf(expectedValueAttribute)
    )

    @Test
    fun toExtAttribute() {
        val extendedAttribute = attribute.toExtAttribute()

        assertThat(extendedAttribute.name).isEqualTo(attribute.name)
        assertThat(extendedAttribute.value).isEqualTo(attribute.value)
        assertNameAttribute(extendedAttribute.nameAttrs)
        assertValueAttribute(extendedAttribute.valueAttrs)
    }

    private fun assertNameAttribute(nameAttrs: List<ExtAttributeDetail>) {
        assertThat(nameAttrs).hasSize(1)

        val nameAttribute = nameAttrs.first()
        assertThat(nameAttribute.name).isEqualTo(expectedNameAttribute.name)
        assertThat(nameAttribute.value).isEqualTo(expectedNameAttribute.value)
    }

    private fun assertValueAttribute(valueAttrs: List<ExtAttributeDetail>) {
        assertThat(valueAttrs).hasSize(1)

        val valueAttribute = valueAttrs.first()
        assertThat(valueAttribute.name).isEqualTo(expectedValueAttribute.name)
        assertThat(valueAttribute.value).isEqualTo(expectedValueAttribute.value)
    }
}
