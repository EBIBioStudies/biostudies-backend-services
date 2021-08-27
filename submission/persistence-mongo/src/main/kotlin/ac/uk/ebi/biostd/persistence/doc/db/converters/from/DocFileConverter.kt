package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocFileConverter(private val docAttributeConverter: DocAttributeConverter) : Converter<Document, DocFile> {
    override fun convert(source: Document): DocFile {
        val attributes = source.getDocList(FILE_DOC_ATTRIBUTES).map { docAttributeConverter.convert(it) }
        val md5 = source.getString(FILE_DOC_MD5)
        val fileSize = source.getLong(FILE_DOC_SIZE)

        return when (source.getString(classField)) {
            FIRE_DOC_FILE_CLASS -> FireDocFile(
                fileName = source.getString(FIRE_FILE_DOC_FILE_NAME),
                fireId = source.getString(FIRE_FILE_DOC_ID),
                attributes = attributes,
                md5 = md5,
                fileSize = fileSize,
            )
            NFS_DOC_FILE_CLASS -> NfsDocFile(
                relPath = source.getString(NFS_FILE_DOC_REL_PATH),
                location = source.getString(NFS_FILE_DOC_FULL_PATH),
                fileType = source.getString(NFS_FILE_TYPE),
                attributes = attributes,
                md5 = md5,
                fileSize = fileSize,
            )
            else -> throw InvalidClassNameDocFileException(source.getString(classField))
        }
    }
}

class InvalidClassNameDocFileException(className: String) : RuntimeException("could not be found $className class")
