package ac.uk.ebi.biostd.persistence.converters

import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AttributeDetailConverterTest {
    private val testInstance = AttributeDetailConverter()

    @Test
    fun convertToDatabaseColumn() {
        val attributes = mutableListOf(AttributeDetail("attr1", "Attribute 1"), AttributeDetail("attr2", "Attribute 2"))
        val convertedAttributes = testInstance.convertToDatabaseColumn(attributes)

        assertThat(convertedAttributes).isEqualTo("attr1=Attribute 1;attr2=Attribute 2")
    }

    @Test
    fun convertToEntityAttribute() {
        val convertedAttributes = testInstance.convertToEntityAttribute("attr1=Attribute 1;attr3=;attr2=Attribute 2")

        assertThat(convertedAttributes).hasSize(2)
        assertThat(convertedAttributes.first()).isEqualTo(AttributeDetail("attr1", "Attribute 1"))
        assertThat(convertedAttributes.second()).isEqualTo(AttributeDetail("attr2", "Attribute 2"))
    }
}
