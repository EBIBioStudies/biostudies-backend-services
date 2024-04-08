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

        assertThat(result[DocAttributeFields.ATTRIBUTE_DOC_NAME]).isEqualTo(DOC_ATTRIBUTE_NAME)
        assertThat(result[DocAttributeFields.ATTRIBUTE_DOC_VALUE]).isEqualTo(DOC_ATTRIBUTE_VALUE)
        assertThat(result[DocAttributeFields.ATTRIBUTE_DOC_REFERENCE]).isEqualTo(true)

        val nameAttributes = result.getAs<List<Document>>(DocAttributeFields.ATTRIBUTE_DOC_NAME_ATTRS)
        val nameAttribute = nameAttributes.first()
        assertThat(nameAttribute[DocAttributeFields.ATTRIBUTE_DETAIL_NAME]).isEqualTo(DOC_NAME_ATTRIBUTE_NAME)
        assertThat(nameAttribute[DocAttributeFields.ATTRIBUTE_DETAIL_VALUE]).isEqualTo(DOC_NAME_ATTRIBUTE_VALUE)

        val valueAttributes = result.getAs<List<Document>>(DocAttributeFields.ATTRIBUTE_DOC_VALUE_ATTRS)
        val valueAttribute = valueAttributes.first()
        assertThat(valueAttribute[DocAttributeFields.ATTRIBUTE_DETAIL_NAME]).isEqualTo(DOC_VALUE_ATTRIBUTE_NAME)
        assertThat(valueAttribute[DocAttributeFields.ATTRIBUTE_DETAIL_VALUE]).isEqualTo(DOC_VALUE_ATTRIBUTE_VALUE)
    }

    private fun createDocAttribute(): DocAttribute {
        return DocAttribute(
            name = DOC_ATTRIBUTE_NAME,
            value = DOC_ATTRIBUTE_VALUE,
            reference = true,
            nameAttrs = docAttributeNameAttrs,
            valueAttrs = docAttributeValueAttrs,
        )
    }

    private companion object {
        const val DOC_ATTRIBUTE_NAME = "docAttributeName"
        const val DOC_ATTRIBUTE_VALUE = "docAttributeValue"

        const val DOC_NAME_ATTRIBUTE_NAME = "name-attr-name"
        const val DOC_NAME_ATTRIBUTE_VALUE = "name-attr-value"

        const val DOC_VALUE_ATTRIBUTE_NAME = "value-attr-name"
        const val DOC_VALUE_ATTRIBUTE_VALUE = "value-attr-value"

        val docAttributeNameAttrs = listOf(DocAttributeDetail(DOC_NAME_ATTRIBUTE_NAME, DOC_NAME_ATTRIBUTE_VALUE))
        val docAttributeValueAttrs = listOf(DocAttributeDetail(DOC_VALUE_ATTRIBUTE_NAME, DOC_VALUE_ATTRIBUTE_VALUE))
    }
}
