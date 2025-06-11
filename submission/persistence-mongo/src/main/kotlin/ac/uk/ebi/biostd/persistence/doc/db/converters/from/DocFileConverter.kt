package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILENAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5_CALCULATED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.CLASS_FIELD
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocFileConverter(
    private val docAttributeConverter: DocAttributeConverter,
) : Converter<Document, DocFile> {
    override fun convert(source: Document): DocFile {
        val fileName = source.getString(FILE_DOC_FILENAME)
        val filePath = source.getString(FILE_DOC_FILEPATH)
        val relPath = source.getString(FILE_DOC_REL_PATH)
        val attributes = source.getDocList(FILE_DOC_ATTRIBUTES).map { docAttributeConverter.convert(it) }
        val md5 = source.getString(FILE_DOC_MD5)
        val fileSize = source.getLong(FILE_DOC_SIZE)
        val fileType = source.getString(FILE_DOC_TYPE)

        return when (source.getString(CLASS_FIELD)) {
            FIRE_DOC_FILE_CLASS ->
                FireDocFile(
                    fileName = fileName,
                    filePath = filePath,
                    relPath = relPath,
                    fireId = source.getString(FIRE_FILE_DOC_ID),
                    attributes = attributes,
                    md5 = md5,
                    fileSize = fileSize,
                    fileType = fileType,
                )

            NFS_DOC_FILE_CLASS ->
                NfsDocFile(
                    fileName = fileName,
                    filePath = filePath,
                    relPath = relPath,
                    fullPath = source.getString(NFS_FILE_FULL_PATH),
                    attributes = attributes,
                    md5 = md5,
                    fileSize = fileSize,
                    md5Calculated = source.getBoolean(FILE_DOC_MD5_CALCULATED),
                    fileType = fileType,
                )

            else -> throw InvalidClassNameDocFileException(source.getString(CLASS_FIELD))
        }
    }
}

class InvalidClassNameDocFileException(
    className: String,
) : RuntimeException("could not be found $className class")
