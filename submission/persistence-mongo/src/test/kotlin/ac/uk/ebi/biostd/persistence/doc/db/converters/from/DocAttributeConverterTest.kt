package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.AttributeConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
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

        assertThat(result).isInstanceOf(docAttributeClass)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.value).isEqualTo(value)
        assertThat(result.reference).isEqualTo(reference)
        assertThat(result.nameAttrs[0]).isInstanceOf(docAttributeDetailClass)
        assertThat(result.nameAttrs[0].name).isEqualTo(attrDetailName1)
        assertThat(result.nameAttrs[0].value).isEqualTo(attrDetailValue1)
        assertThat(result.valueAttrs[0].name).isEqualTo(attrDetailName2)
        assertThat(result.valueAttrs[0].value).isEqualTo(attrDetailValue2)
    }

    private fun createAttributeDoc(): Document {
        val attributeDoc = Document()
        attributeDoc[classField] = docAttributeClass
        attributeDoc[DocAttributeConverter.docAttributeName] = name
        attributeDoc[DocAttributeConverter.docAttributeValue] = value
        attributeDoc[DocAttributeConverter.docAttributeReference] = reference
        attributeDoc[DocAttributeConverter.docAttributeNameAttrs] = listOf(createAttributeDetailDoc(attrDetailName1, attrDetailValue1))
        attributeDoc[DocAttributeConverter.docAttributeValueAttrs] = listOf(createAttributeDetailDoc(attrDetailName2, attrDetailValue2))
        return attributeDoc
    }

    private fun createAttributeDetailDoc(name: String, value: String): Document {
        val attributeDetailDoc = Document()
        attributeDetailDoc[classField] = docAttributeDetailClass
        attributeDetailDoc[AttributeConverter.attributeDetailName] = name
        attributeDetailDoc[AttributeConverter.attributeDetailValue] = value
        return attributeDetailDoc
    }

    companion object {
        val docAttributeClass = DocAttribute::class.java
        val docAttributeDetailClass = DocAttributeDetail::class.java
        const val name = "name"
        const val value = "value"
        const val reference = false
        const val attrDetailName1 = "name1"
        const val attrDetailValue1 = "value1"
        const val attrDetailName2 = "name2"
        const val attrDetailValue2 = "value2"
    }

}
