package ebi.ac.uk.model.extensions

import ebi.ac.uk.io.sources.ConfiguredDbFile
import ebi.ac.uk.io.sources.DbFile
import ebi.ac.uk.io.sources.UploadedDbFile
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.FileFields

val BioFile.extension: String
    get() = path.substringAfterLast(".")

val BioFile.md5: String?
    get() = find(FileFields.DB_MD5)

val BioFile.dbFile: DbFile?
    get() {
        val dbMd5 = find(FileFields.DB_MD5)
        if (dbMd5 != null) {
            val size = find(FileFields.DB_SIZE)
            val id = find(FileFields.DB_ID)
            val path = find(FileFields.DB_PATH)
            val published = find(FileFields.DB_PUBLISHED)
            return if (size != null && id != null && path != null)
                ConfiguredDbFile(id, dbMd5, path, size.toLong(), published.toBoolean())
            else
                UploadedDbFile(dbMd5)
        }
        return null
    }
