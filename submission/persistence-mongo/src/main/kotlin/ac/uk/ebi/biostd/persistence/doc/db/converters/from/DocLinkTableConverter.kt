package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkTableFields.LINK_TABLE_DOC_LINKS
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocLinkTableConverter(private val docLinkConverter: DocLinkConverter) : Converter<Document, DocLinkTable> {
    override fun convert(source: Document): DocLinkTable =
        DocLinkTable(
            source.getDocList(LINK_TABLE_DOC_LINKS).map { docLinkConverter.convert(it) },
        )
}
