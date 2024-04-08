package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DETAIL_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DETAIL_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.DOC_ATTRIBUTE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.CLASS_FIELD
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class AttributeConverter : Converter<DocAttribute, Document> {
    override fun convert(attribute: DocAttribute): Document {
        val attributeDoc = Document()
        attributeDoc[CLASS_FIELD] = DOC_ATTRIBUTE_CLASS
        attributeDoc[ATTRIBUTE_DOC_NAME] = attribute.name
        attributeDoc[ATTRIBUTE_DOC_VALUE] = attribute.value
        attributeDoc[ATTRIBUTE_DOC_REFERENCE] = attribute.reference
        attributeDoc[ATTRIBUTE_DOC_NAME_ATTRS] = attribute.nameAttrs.map { toDocument(it) }
        attributeDoc[ATTRIBUTE_DOC_VALUE_ATTRS] = attribute.valueAttrs.map { toDocument(it) }
        return attributeDoc
    }

    private fun toDocument(docAttributeDetail: DocAttributeDetail): Document {
        val attributeDetailDoc = Document()
        attributeDetailDoc[ATTRIBUTE_DETAIL_NAME] = docAttributeDetail.name
        attributeDetailDoc[ATTRIBUTE_DETAIL_VALUE] = docAttributeDetail.value
        return attributeDetailDoc
    }
}
