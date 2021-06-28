package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.FIRE
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.NFS
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocFileConverter(private val docAttributeConverter: DocAttributeConverter) : Converter<Document, DocFile> {
    override fun convert(source: Document): DocFile {
        return when (source.getString(classField)) {
            FIRE_DOC_FILE_CLASS -> FireDocFile(
                relPath = source.getString(FIRE_FILE_DOC_REL_PATH),
                fullPath = source.getString(FIRE_FILE_DOC_FULL_PATH),
                fireId = source.getString(FIRE_FILE_DOC_ID),
                attributes = source.getDocList(FIRE_FILE_DOC_ATTRIBUTES).map { docAttributeConverter.convert(it) },
                md5 = source.getString(FIRE_FILE_DOC_MD5),
                fileSize = source.getLong(FIRE_FILE_SIZE),
                fileSystem = FIRE
            )
            NFS_DOC_FILE_CLASS -> NfsDocFile(
                relPath = source.getString(FILE_DOC_REL_PATH),
                fullPath = source.getString(FILE_DOC_FULL_PATH),
                attributes = source.getDocList(FILE_DOC_ATTRIBUTES).map { docAttributeConverter.convert(it) },
                md5 = source.getString(FILE_DOC_MD5),
                fileType = source.getString(FILE_TYPE),
                fileSize = source.getLong(FILE_SIZE),
                fileSystem = NFS
            )
            else -> throw InvalidClassNameDocFile(source.getString(classField))
        }
    }
}

class InvalidClassNameDocFile(className: String) : RuntimeException("could not be found $className class")
