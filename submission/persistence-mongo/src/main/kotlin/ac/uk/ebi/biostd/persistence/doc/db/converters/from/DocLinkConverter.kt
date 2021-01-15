package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkFields.LINK_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkFields.LINK_DOC_URL
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocLinkConverter(private val docAttributeConverter: DocAttributeConverter) : Converter<Document, DocLink> {
    override fun convert(source: Document): DocLink = DocLink(
        url = source.getString(LINK_DOC_URL),
        attributes = source.getDocList(LINK_DOC_ATTRIBUTES).map { docAttributeConverter.convert(it) }
    )
}
