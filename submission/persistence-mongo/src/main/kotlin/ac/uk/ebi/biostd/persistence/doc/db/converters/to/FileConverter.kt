package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileConverter(private val attributeConverter: AttributeConverter) : Converter<DocFile, Document> {
    override fun convert(docFile: DocFile): Document {
        val file = Document()
        file[classField] = DOC_FILE_CLASS
        file[FILE_DOC_REL_PATH] = docFile.relPath
        file[FILE_DOC_FULL_PATH] = docFile.fullPath
        file[FILE_DOC_ATTRIBUTES] = docFile.attributes.map { attributeConverter.convert(it) }
        file[FILE_DOC_MD5] = docFile.md5
        return file
    }
}
