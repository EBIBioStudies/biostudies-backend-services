package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.AttributeDetail
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToAttributeTest {

    private val expectedNameAttribute = ExtAttributeDetail(name = "Name Attribute Name", value = "Name Attribute Value")
    private val expectedValueAttribute = ExtAttributeDetail(name = "Value Attribute Name", value = "Value Attribute Value")

    private val extAttribute = ExtAttribute(
        name = "Attribute Name",
        value = "Attribute Value",
        reference = true,
        nameAttrs = mutableListOf(expectedNameAttribute),
        valueAttrs = mutableListOf(expectedValueAttribute)
    )

    @Test
    fun toExtAttribute() {
        val attribute = extAttribute.toAttribute()

        assertThat(attribute.name).isEqualTo(extAttribute.name)
        assertThat(attribute.value).isEqualTo(extAttribute.value)
        assertNameAttribute(attribute.nameAttrs)
        assertValueAttribute(attribute.valueAttrs)
    }

    private fun assertNameAttribute(nameAttrs: List<AttributeDetail>) {
        assertThat(nameAttrs).hasSize(1)

        val nameAttribute = nameAttrs.first()
        assertThat(nameAttribute.name).isEqualTo(expectedNameAttribute.name)
        assertThat(nameAttribute.value).isEqualTo(expectedNameAttribute.value)
    }

    private fun assertValueAttribute(valueAttrs: List<AttributeDetail>) {
        assertThat(valueAttrs).hasSize(1)

        val valueAttribute = valueAttrs.first()
        assertThat(valueAttribute.name).isEqualTo(expectedValueAttribute.name)
        assertThat(valueAttribute.value).isEqualTo(expectedValueAttribute.value)
    }
}
