package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocLinkConverter(private val docAttributeConverter: DocAttributeConverter) : Converter<Document, DocLink> {
    override fun convert(source: Document): DocLink {
        return DocLink(
            url = source.getString(docLinkUrl),
            attributes = source.getDocList(docLinkAttributes).map { docAttributeConverter.convert(it) }
        )
    }

    companion object {
        const val docLinkUrl = "url"
        const val docLinkAttributes = "attributes"
    }
}
