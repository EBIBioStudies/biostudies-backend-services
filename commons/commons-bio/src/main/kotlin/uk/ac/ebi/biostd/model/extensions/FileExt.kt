package uk.ac.ebi.biostd.model.extensions

import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.constants.FileFields

val BioFile.extension: String
    get() = path.substringAfterLast(".")

val BioFile.md5: String?
    get() = find(FileFields.DB_MD5)
