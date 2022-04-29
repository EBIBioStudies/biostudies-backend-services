package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.FileFields

val BioFile.extension: String
    get() = path.substringAfterLast(".")

val BioFile.md5: String?
    get() = find(FileFields.MD5)
