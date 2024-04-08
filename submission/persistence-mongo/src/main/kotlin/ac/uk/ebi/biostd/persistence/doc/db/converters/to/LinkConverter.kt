package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkFields.DOC_LINK_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkFields.LINK_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkFields.LINK_DOC_URL
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.CLASS_FIELD
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class LinkConverter(private val attributeConverter: AttributeConverter) : Converter<DocLink, Document> {
    override fun convert(link: DocLink): Document {
        val linkDoc = Document()
        linkDoc[CLASS_FIELD] = DOC_LINK_CLASS
        linkDoc[LINK_DOC_URL] = link.url
        linkDoc[LINK_DOC_ATTRIBUTES] = link.attributes.map { attributeConverter.convert(it) }
        return linkDoc
    }
}
