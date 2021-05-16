package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields
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

        assertThat(result[DocAttributeFields.ATTRIBUTE_DOC_NAME]).isEqualTo(docAttributeName)
        assertThat(result[DocAttributeFields.ATTRIBUTE_DOC_VALUE]).isEqualTo(docAttributeValue)
        assertThat(result[DocAttributeFields.ATTRIBUTE_DOC_REFERENCE]).isEqualTo(true)

        val nameAttributes = result.getAs<List<Document>>(DocAttributeFields.ATTRIBUTE_DOC_NAME_ATTRS)
        val nameAttribute = nameAttributes.first()
        assertThat(nameAttribute[DocAttributeFields.ATTRIBUTE_DETAIL_NAME]).isEqualTo(docNameAttributeName)
        assertThat(nameAttribute[DocAttributeFields.ATTRIBUTE_DETAIL_VALUE]).isEqualTo(docNameAttributeValue)

        val valueAttributes = result.getAs<List<Document>>(DocAttributeFields.ATTRIBUTE_DOC_VALUE_ATTRS)
        val valueAttribute = valueAttributes.first()
        assertThat(valueAttribute[DocAttributeFields.ATTRIBUTE_DETAIL_NAME]).isEqualTo(docValueAttributeName)
        assertThat(valueAttribute[DocAttributeFields.ATTRIBUTE_DETAIL_VALUE]).isEqualTo(docValueAttributeValue)
    }

    private fun createDocAttribute(): DocAttribute {
        return DocAttribute(
            name = docAttributeName,
            value = docAttributeValue,
            reference = true,
            nameAttrs = docAttributeNameAttrs,
            valueAttrs = docAttributeValueAttrs
        )
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
