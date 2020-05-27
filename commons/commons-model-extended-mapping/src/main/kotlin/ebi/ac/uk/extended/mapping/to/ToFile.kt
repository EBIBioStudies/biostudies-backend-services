package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.File

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(): File = File(fileName, file.size(), attributes.mapTo(mutableListOf()) { it.toAttribute() })
