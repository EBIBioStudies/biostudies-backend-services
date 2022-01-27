package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_FILE_FILENAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_PAGE_TAB_FILES
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocFileListConverter(
    private val docFileRefConverter: DocFileRefConverter,
    private val docFileConverter: DocFileConverter
) : Converter<Document, DocFileList> {
    override fun convert(source: Document): DocFileList = DocFileList(
        fileName = source.getString(FILE_LIST_DOC_FILE_FILENAME),
        pageTabFiles = source.getDocList(FILE_LIST_DOC_PAGE_TAB_FILES).map { docFileConverter.convert(it) }
    )
}
