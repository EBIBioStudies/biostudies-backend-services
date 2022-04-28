package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields

val File.extension: String
    get() = path.substringAfterLast(".")

val File.md5: String?
    get() = find(FileFields.MD5)
