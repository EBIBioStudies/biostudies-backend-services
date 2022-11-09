package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.BioFile

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(): BioFile =
    when (this) {
        is NfsFile -> BioFile(filePath, file.size(), type.value, generateAttributes(attributes))
        is FireFile -> BioFile(filePath, size, type.value, generateAttributes(attributes))
    }

private fun generateAttributes(extAttrs: List<ExtAttribute>) = extAttrs.map { it.toAttribute() }
