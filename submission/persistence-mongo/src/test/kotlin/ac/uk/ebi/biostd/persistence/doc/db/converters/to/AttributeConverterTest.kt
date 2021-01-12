package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test

internal class AttributeConverterTest {

    private val testInstance = AttributeConverter()

    @Test
    fun convert() {
        val docAttribute = createDocAttribute()

        val result = testInstance.convert(docAttribute)

        assertThat(result[AttributeConverter.attributeDocName]).isEqualTo(docAttributeName)
        assertThat(result[AttributeConverter.attributeDocValue]).isEqualTo(docAttributeValue)
        assertThat(result[AttributeConverter.attributeDocReference]).isEqualTo(true)

        val nameAttributes = result[AttributeConverter.attributeDocNameAttrs] as List<Document>
        val nameAttribute = nameAttributes.first()
        assertThat(nameAttribute[AttributeConverter.attributeDetailName]).isEqualTo(docNameAttributeName)
        assertThat(nameAttribute[AttributeConverter.attributeDetailValue]).isEqualTo(docNameAttributeValue)

        val valueAttributes = result[AttributeConverter.attributeDocValueAttrs] as List<Document>
        val valueAttribute = valueAttributes.first()
        assertThat(valueAttribute[AttributeConverter.attributeDetailName]).isEqualTo(docValueAttributeName)
        assertThat(valueAttribute[AttributeConverter.attributeDetailValue]).isEqualTo(docValueAttributeValue)
    }

    private fun createDocAttribute(): DocAttribute {
        return DocAttribute(
            name = docAttributeName,
            value = docAttributeValue,
            reference = true,
            nameAttrs = docAttributeNameAttrs,
            valueAttrs = docAttributeValueAttrs)
    }

    private companion object {
        const val docAttributeName = "docAttributeName"
        const val docAttributeValue = "docAttributeValue"

        const val docNameAttributeName = "name-attr-name"
        const val docNameAttributeValue = "name-attr-value"

        const val docValueAttributeName = "value-attr-name"
        const val docValueAttributeValue = "value-attr-value"

        val docAttributeNameAttrs = listOf(DocAttributeDetail(docNameAttributeName, docNameAttributeValue))
        val docAttributeValueAttrs = listOf(DocAttributeDetail(docValueAttributeName, docValueAttributeValue))
    }
}
