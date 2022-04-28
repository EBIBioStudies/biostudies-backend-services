package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.File

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(): File =
    when (this) {
        is NfsFile -> File(filePath, file.size(), type.value, attributes.mapTo(mutableListOf()) { it.toAttribute() })
        is FireFile -> File(filePath, size, type.value, attributes.mapTo(mutableListOf()) { it.toAttribute() })
    }
