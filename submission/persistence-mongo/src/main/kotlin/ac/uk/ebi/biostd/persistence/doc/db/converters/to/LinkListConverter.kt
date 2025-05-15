package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkListFields.DOC_LINK_LIST_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkListFields.LINK_LIST_DOC_FILE_FILENAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkListFields.LINK_LIST_DOC_PAGE_TAB_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.CLASS_FIELD
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import org.bson.Document

class LinkListConverter(
    private val fileConverter: FileConverter,
) {
    fun convert(docLinkList: DocLinkList): Document {
        val linkList = Document()
        linkList[CLASS_FIELD] = DOC_LINK_LIST_CLASS
        linkList[LINK_LIST_DOC_FILE_FILENAME] = docLinkList.fileName
        linkList[LINK_LIST_DOC_PAGE_TAB_FILES] = docLinkList.pageTabFiles.map { fileConverter.convert(it) }

        return linkList
    }
}
