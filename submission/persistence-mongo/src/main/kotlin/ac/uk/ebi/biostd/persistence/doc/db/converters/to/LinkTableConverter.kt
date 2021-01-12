package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class LinkTableConverter(private val linkConverter: LinkConverter) : Converter<DocLinkTable, Document> {
    override fun convert(linkTable: DocLinkTable): Document {
        val linkTableDoc = Document()
        linkTableDoc[classField] = clazz
        linkTableDoc[linkTableDocLinks] = linkTable.links.map { linkConverter.convert(it) }
        return linkTableDoc
    }

    companion object {
        val clazz: String = DocLinkTable::class.java.canonicalName
        const val linkTableDocLinks = "links"
    }
}
