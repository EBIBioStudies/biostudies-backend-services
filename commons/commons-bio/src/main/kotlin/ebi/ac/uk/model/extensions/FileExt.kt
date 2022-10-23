package ebi.ac.uk.model.extensions

import ebi.ac.uk.io.sources.ConfiguredFileDb
import ebi.ac.uk.io.sources.FileDb
import ebi.ac.uk.io.sources.UploadedFileDb
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.FileFields

val BioFile.extension: String
    get() = path.substringAfterLast(".")

val BioFile.md5: String?
    get() = find(FileFields.DB_MD5)

val BioFile.fileDb: FileDb?
    get() {
        val dbMd5 = find(FileFields.DB_MD5)
        if (dbMd5 != null) {
            val size = find(FileFields.DB_SIZE)
            val id = find(FileFields.DB_ID)
            val path = find(FileFields.DB_PATH)
            return if (size != null && id != null && path != null)
                ConfiguredFileDb(id, dbMd5, path, size.toLong())
            else
                UploadedFileDb(dbMd5)
        }
        return null
    }
