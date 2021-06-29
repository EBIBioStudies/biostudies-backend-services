package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_FILE_SYSTEM
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_SIZE

class FileConverter(private val attributeConverter: AttributeConverter) : Converter<DocFile, Document> {
    override fun convert(docFile: DocFile): Document {
        val file = Document()
        when (docFile) {
            is FireDocFile -> {
                file[classField] = FIRE_DOC_FILE_CLASS
                file[FIRE_FILE_DOC_REL_PATH] = docFile.relPath
                file[FIRE_FILE_DOC_ID] = docFile.fireId
                file[FIRE_FILE_DOC_ATTRIBUTES] = docFile.attributes.map { attributeConverter.convert(it) }
                file[FIRE_FILE_DOC_MD5] = docFile.md5
                file[FIRE_FILE_SIZE] = docFile.fileSize
                file[FIRE_FILE_DOC_FILE_SYSTEM] = docFile.fileSystem.name
            }
            is NfsDocFile -> {
                file[classField] = NFS_DOC_FILE_CLASS
                file[FILE_DOC_REL_PATH] = docFile.relPath
                file[FILE_DOC_FULL_PATH] = docFile.fullPath
                file[FILE_DOC_ATTRIBUTES] = docFile.attributes.map { attributeConverter.convert(it) }
                file[FILE_DOC_MD5] = docFile.md5
                file[FILE_TYPE] = docFile.fileType
                file[FILE_SIZE] = docFile.fileSize
            }
        }

        return file
    }
}
