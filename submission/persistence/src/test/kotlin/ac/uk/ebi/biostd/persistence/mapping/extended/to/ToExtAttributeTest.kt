package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.nameAttribute
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.valueAttribute
import ac.uk.ebi.biostd.persistence.model.Attribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToExtAttributeTest {
    private val expectedNameAttribute = nameAttribute
    private val expectedValueAttribute = valueAttribute

    private val attribute = Attribute(
        name = "Attribute Name",
        value = "Attribute Value",
        order = 1,
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
