package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkTableFields.DOC_LINK_TABLE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkTableFields.LINK_TABLE_DOC_LINKS
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class LinkTableConverter(private val linkConverter: LinkConverter) : Converter<DocLinkTable, Document> {
    override fun convert(linkTable: DocLinkTable): Document {
        val linkTableDoc = Document()
        linkTableDoc[classField] = DOC_LINK_TABLE_CLASS
        linkTableDoc[LINK_TABLE_DOC_LINKS] = linkTable.links.map { linkConverter.convert(it) }
        return linkTableDoc
    }
}
