package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.File

val File.extension: String
    get() = path.substringAfterLast(".")
