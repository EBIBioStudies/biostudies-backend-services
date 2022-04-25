package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILENAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_DOC_DIRECTORY_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_SOURCE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.FireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileConverter(private val attributeConverter: AttributeConverter) : Converter<DocFile, Document> {
    override fun convert(docFile: DocFile): Document {
        val file = Document()

        file[FILE_DOC_FILENAME] = docFile.fileName
        file[FILE_DOC_FILEPATH] = docFile.filePath
        file[FILE_DOC_REL_PATH] = docFile.relPath
        file[FILE_DOC_ATTRIBUTES] = docFile.attributes.map { attributeConverter.convert(it) }
        file[FILE_DOC_MD5] = docFile.md5
        file[FILE_DOC_SIZE] = docFile.fileSize

        when (docFile) {
            is FireDocFile -> {
                file[classField] = FIRE_DOC_FILE_CLASS
                file[FIRE_FILE_DOC_ID] = docFile.fireId
            }
            is FireDocDirectory -> {
                file[classField] = FIRE_DOC_DIRECTORY_CLASS
                file[FIRE_FILE_DOC_ID] = docFile.fireId
            }
            is NfsDocFile -> {
                file[classField] = NFS_DOC_FILE_CLASS
                file[NFS_FILE_FULL_PATH] = docFile.fullPath
                file[NFS_FILE_TYPE] = docFile.fileType
                file[NFS_FILE_SOURCE] = docFile.source
            }
        }

        return file
    }
}
