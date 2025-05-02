package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkListFields.LINK_LIST_DOC_FILE_FILENAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkListFields.LINK_LIST_DOC_PAGE_TAB_FILES
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocLinkListConverter(
    private val docFileConverter: DocFileConverter,
) : Converter<Document, DocLinkList> {
    override fun convert(source: Document): DocLinkList =
        DocLinkList(
            fileName = source.getString(LINK_LIST_DOC_FILE_FILENAME),
            pageTabFiles = source.getDocList(LINK_LIST_DOC_PAGE_TAB_FILES).map { docFileConverter.convert(it) },
        )
}
