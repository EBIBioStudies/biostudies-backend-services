package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.CLASS_FIELD
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test

internal class DocAttributeConverterTest {
    private val testInstance = DocAttributeConverter()

    @Test
    fun convert() {
        val result = testInstance.convert(createAttributeDoc())

        assertThat(result).isInstanceOf(DocAttribute::class.java)
        assertThat(result.name).isEqualTo(NAME)
        assertThat(result.value).isEqualTo(VALUE)
        assertThat(result.reference).isEqualTo(REFERENCE)
        assertThat(result.nameAttrs[0]).isInstanceOf(DocAttributeDetail::class.java)
        assertThat(result.nameAttrs[0].name).isEqualTo(ATTR_DETAIL_NAME_1)
        assertThat(result.nameAttrs[0].value).isEqualTo(ATTR_DETAIL_VALUE_1)
        assertThat(result.valueAttrs[0].name).isEqualTo(ATTR_DETAIL_NAME_2)
        assertThat(result.valueAttrs[0].value).isEqualTo(ATTR_DETAIL_VALUE_2)
    }

    private fun createAttributeDoc(): Document {
        val attributeDoc = Document()
        attributeDoc[CLASS_FIELD] = DocAttribute::class.java
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_NAME] = NAME
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_VALUE] = VALUE
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_REFERENCE] = REFERENCE
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_NAME_ATTRS] =
            listOf(createAttributeDetailDoc(ATTR_DETAIL_NAME_1, ATTR_DETAIL_VALUE_1))
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_VALUE_ATTRS] =
            listOf(createAttributeDetailDoc(ATTR_DETAIL_NAME_2, ATTR_DETAIL_VALUE_2))
        return attributeDoc
    }

    private fun createAttributeDetailDoc(
        name: String,
        value: String,
    ): Document {
        val attributeDetailDoc = Document()
        attributeDetailDoc[CLASS_FIELD] = DocAttributeDetail::class.java
        attributeDetailDoc[DocAttributeFields.ATTRIBUTE_DETAIL_NAME] = name
        attributeDetailDoc[DocAttributeFields.ATTRIBUTE_DETAIL_VALUE] = value
        return attributeDetailDoc
    }

    companion object {
        const val NAME = "name"
        const val VALUE = "value"
        const val REFERENCE = false
        const val ATTR_DETAIL_NAME_1 = "name1"
        const val ATTR_DETAIL_VALUE_1 = "value1"
        const val ATTR_DETAIL_NAME_2 = "name2"
        const val ATTR_DETAIL_VALUE_2 = "value2"
    }
}
