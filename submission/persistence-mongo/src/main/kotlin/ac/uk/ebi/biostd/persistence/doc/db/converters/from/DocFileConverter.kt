package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_TYPE
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocFileConverter(private val docAttributeConverter: DocAttributeConverter) : Converter<Document, DocFile> {
    override fun convert(source: Document): DocFile = DocFile(
        relPath = source.getString(FILE_DOC_REL_PATH),
        fullPath = source.getString(FILE_DOC_FULL_PATH),
        attributes = source.getDocList(FILE_DOC_ATTRIBUTES).map { docAttributeConverter.convert(it) },
        md5 = source.getString(FILE_DOC_MD5),
        fileType = source.getString(FILE_TYPE),
        fileSize = source.getLong(FILE_SIZE)
    )
}
