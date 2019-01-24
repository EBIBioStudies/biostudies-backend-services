package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields

var File.size: Int
    get() = this[FileFields.SIZE]
    set(value) {
        this[FileFields.SIZE] = value
    }

var File.type: String
    get() = this[FileFields.TYPE]
    set(value) {
        this[FileFields.TYPE] = value
    }
