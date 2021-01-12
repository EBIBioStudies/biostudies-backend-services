package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocAttributeConverter : Converter<Document, DocAttribute> {
    override fun convert(source: Document): DocAttribute {
        return DocAttribute(
            name = source.getString(docAttributeName),
            value = source.getString(docAttributeValue),
            reference = source.getBoolean(docAttributeReference, false),
            nameAttrs = source.getDocList(docAttributeNameAttrs).map { toAttributeDetail(it) },
            valueAttrs = source.getDocList(docAttributeValueAttrs).map { toAttributeDetail(it) }
        )
    }

    private fun toAttributeDetail(doc: Document): DocAttributeDetail =
        DocAttributeDetail(name = doc.getString(attributeDetailName), value = doc.getString(attributeDetailValue))

    companion object {
        const val docAttributeName = "name"
        const val docAttributeValue = "value"
        const val docAttributeNameAttrs = "nameAttrs"
        const val docAttributeReference = "reference"
        const val docAttributeValueAttrs = "valueAttrs"
        const val attributeDetailName = "name"
        const val attributeDetailValue = "value"
    }
}
