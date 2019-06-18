package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.File

fun ExtFile.toFile(): File = File(fileName, file.length(), attributes.map { it.toAttribute() })
