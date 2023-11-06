package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.BioFile

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

/**
 * The file size for inner directories is not calculated in order to avoid timing out requests for submissions that
 * include big sized directories. @see https://www.pivotaltracker.com/story/show/185900074
 */
fun ExtFile.toFile(): BioFile {
    return when (this) {
        is NfsFile ->
            BioFile(filePath, file.size(calculateDirectories = false), type.value, attributes.map { it.toAttribute() })

        is FireFile -> BioFile(filePath, size, type.value, attributes.map { it.toAttribute() })
    }
}
