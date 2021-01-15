package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import org.bson.Document
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DocAttributeConverterTest {
    private val testInstance = DocAttributeConverter()

    @Test
    fun convert() {
        val result = testInstance.convert(createAttributeDoc())

        assertThat(result).isInstanceOf(DocAttribute::class.java)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.value).isEqualTo(value)
        assertThat(result.reference).isEqualTo(reference)
        assertThat(result.nameAttrs[0]).isInstanceOf(DocAttributeDetail::class.java)
        assertThat(result.nameAttrs[0].name).isEqualTo(attrDetailName1)
        assertThat(result.nameAttrs[0].value).isEqualTo(attrDetailValue1)
        assertThat(result.valueAttrs[0].name).isEqualTo(attrDetailName2)
        assertThat(result.valueAttrs[0].value).isEqualTo(attrDetailValue2)
    }

    private fun createAttributeDoc(): Document {
        val attributeDoc = Document()
        attributeDoc[classField] = DocAttribute::class.java
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_NAME] = name
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_VALUE] = value
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_REFERENCE] = reference
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_NAME_ATTRS] = listOf(createAttributeDetailDoc(attrDetailName1, attrDetailValue1))
        attributeDoc[DocAttributeFields.ATTRIBUTE_DOC_VALUE_ATTRS] = listOf(createAttributeDetailDoc(attrDetailName2, attrDetailValue2))
        return attributeDoc
    }

    private fun createAttributeDetailDoc(name: String, value: String): Document {
        val attributeDetailDoc = Document()
        attributeDetailDoc[classField] = DocAttributeDetail::class.java
        attributeDetailDoc[DocAttributeFields.ATTRIBUTE_DETAIL_NAME] = name
        attributeDetailDoc[DocAttributeFields.ATTRIBUTE_DETAIL_VALUE] = value
        return attributeDetailDoc
    }

    companion object {
        const val name = "name"
        const val value = "value"
        const val reference = false
        const val attrDetailName1 = "name1"
        const val attrDetailValue1 = "value1"
        const val attrDetailName2 = "name2"
        const val attrDetailValue2 = "value2"
    }
}
