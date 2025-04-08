package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.RequestFile
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

/**
 * The file size for inner directories is not calculated in order to avoid timing out requests for submissions that
 * include big sized directories. @see https://www.pivotaltracker.com/story/show/185900074
 */
fun ExtFile.toFile(): BioFile =
    when (this) {
        is NfsFile -> BioFile(filePath, size, type.value, attributes.filterAttributes())
        is FireFile -> BioFile(filePath, size, type.value, attributes.filterAttributes())
        is RequestFile -> error("RequestFile $filePath does not identify explicit File instance")
    }

private fun List<ExtAttribute>.filterAttributes() = filterNot { FILES_RESERVED_ATTRS.contains(it.name) }.map { it.toAttribute() }
