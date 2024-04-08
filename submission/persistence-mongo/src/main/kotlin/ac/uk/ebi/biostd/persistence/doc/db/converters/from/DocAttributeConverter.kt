package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DETAIL_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DETAIL_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocAttributeConverter : Converter<Document, DocAttribute> {
    override fun convert(source: Document): DocAttribute =
        DocAttribute(
            name = source.getString(ATTRIBUTE_DOC_NAME),
            value = source.getString(ATTRIBUTE_DOC_VALUE),
            reference = source.getBoolean(ATTRIBUTE_DOC_REFERENCE, false),
            nameAttrs = source.getDocList(ATTRIBUTE_DOC_NAME_ATTRS).map { toAttributeDetail(it) },
            valueAttrs = source.getDocList(ATTRIBUTE_DOC_VALUE_ATTRS).map { toAttributeDetail(it) },
        )

    private fun toAttributeDetail(doc: Document): DocAttributeDetail =
        DocAttributeDetail(name = doc.getString(ATTRIBUTE_DETAIL_NAME), value = doc.getString(ATTRIBUTE_DETAIL_VALUE))
}
