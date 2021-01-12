package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class AttributeConverter : Converter<DocAttribute, Document> {
    override fun convert(attribute: DocAttribute): Document {
        val attributeDoc = Document()
        attributeDoc[classField] = clazz
        attributeDoc[attributeDocName] = attribute.name
        attributeDoc[attributeDocValue] = attribute.value
        attributeDoc[attributeDocReference] = attribute.reference
        attributeDoc[attributeDocNameAttrs] = attribute.nameAttrs.map { toDocument(it) }
        attributeDoc[attributeDocValueAttrs] = attribute.valueAttrs.map { toDocument(it) }
        return attributeDoc
    }

    private fun toDocument(docAttributeDetail: DocAttributeDetail): Document {
        val attributeDetailDoc = Document()
        attributeDetailDoc[attributeDetailName] = docAttributeDetail.name
        attributeDetailDoc[attributeDetailValue] = docAttributeDetail.value
        return attributeDetailDoc
    }

    companion object {
        val clazz: String = DocAttribute::class.java.canonicalName
        const val attributeDocName = "name"
        const val attributeDocValue = "value"
        const val attributeDocNameAttrs = "nameAttrs"
        const val attributeDocReference = "reference"
        const val attributeDocValueAttrs = "valueAttrs"
        const val attributeDetailName = "name"
        const val attributeDetailValue = "value"
    }
}
