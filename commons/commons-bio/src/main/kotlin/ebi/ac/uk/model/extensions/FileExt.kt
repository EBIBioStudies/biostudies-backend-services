package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields

var File.type: String
    get() = this[FileFields.TYPE]
    set(value) {
        this[FileFields.TYPE] = value
    }

val File.extension: String
    get() = path.substringAfterLast(".")
