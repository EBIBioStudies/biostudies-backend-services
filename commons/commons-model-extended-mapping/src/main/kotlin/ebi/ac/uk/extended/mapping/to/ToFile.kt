package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.biostd.extended.model.NfsFile
import uk.ac.ebi.biostd.extended.model.RequestFile

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(): BioFile =
    when (this) {
        is NfsFile -> BioFile(filePath, size, type.value, attributes.filterAttributes())
        is FireFile -> BioFile(filePath, size, type.value, attributes.filterAttributes())
        is RequestFile -> error("RequestFile $filePath does not identify explicit File instance")
    }

private fun List<ExtAttribute>.filterAttributes() = filterNot { FILES_RESERVED_ATTRS.contains(it.name) }.map { it.toAttribute() }
