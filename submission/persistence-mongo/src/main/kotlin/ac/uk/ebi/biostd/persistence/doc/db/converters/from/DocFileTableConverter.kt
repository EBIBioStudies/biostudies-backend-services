package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocFileTableConverter(private val docFileConverter: DocFileConverter) : Converter<Document, DocFileTable> {
    override fun convert(source: Document): DocFileTable {
        return DocFileTable(
            files = source.getDocList(docFileTableFiles).map { docFileConverter.convert(it) }
        )
    }

    companion object {
        const val docFileTableFiles = "files"
    }
}
