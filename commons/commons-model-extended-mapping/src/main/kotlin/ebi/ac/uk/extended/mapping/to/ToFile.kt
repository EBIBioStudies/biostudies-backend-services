package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.BioFile

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(calculateDirectories: Boolean = true): BioFile {
    return when (this) {
        is NfsFile ->
            BioFile(filePath, file.size(calculateDirectories), type.value, attributes.map { it.toAttribute() })

        is FireFile -> BioFile(filePath, size, type.value, attributes.map { it.toAttribute() })
    }
}
