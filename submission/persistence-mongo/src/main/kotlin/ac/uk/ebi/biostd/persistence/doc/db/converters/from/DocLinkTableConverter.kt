package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocLinkTableConverter(private val docLinkConverter: DocLinkConverter) : Converter<Document, DocLinkTable> {
    override fun convert(source: Document): DocLinkTable {
        return DocLinkTable(
            links = source.getDocList(docLinkTableLinks).map { docLinkConverter.convert(it) }
        )
    }

    companion object {
        const val docLinkTableLinks = "links"
    }
}
