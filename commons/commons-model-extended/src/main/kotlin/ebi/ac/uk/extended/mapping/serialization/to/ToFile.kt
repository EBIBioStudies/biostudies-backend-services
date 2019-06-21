package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.File

fun ExtFile.toFile(): File = File(fileName, file.length(), attributes.mapTo(mutableListOf()) { it.toAttribute() })

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.to.ToFileKt"
