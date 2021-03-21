package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileRefFields
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import org.bson.Document

class FileRefConverter {

    fun convert(docFileRef: DocFileRef): Document {
        val fileList = Document()
        fileList[CommonsConverter.classField] = DocFileRefFields.DOC_FILE_REF_CLASS
        fileList[DocFileRefFields.FILE_REF_DOC_FILE_ID] = docFileRef.fileId.toString()
        return fileList
    }
}
