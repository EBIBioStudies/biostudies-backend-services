package uk.ac.ebi.biostd.extended.mapping.to

import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.biostd.extended.model.NfsFile
import uk.ac.ebi.biostd.extended.model.RequestFile
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.constants.FILES_RESERVED_ATTRS

internal const val TO_FILE_EXTENSIONS = "uk.ac.ebi.biostd.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(): BioFile =
    when (this) {
        is NfsFile -> BioFile(filePath, size, type.value, attributes.filterAttributes())
        is FireFile -> BioFile(filePath, size, type.value, attributes.filterAttributes())
        is RequestFile -> error("RequestFile $filePath does not identify explicit File instance")
    }

private fun List<ExtAttribute>.filterAttributes() = filterNot { FILES_RESERVED_ATTRS.contains(it.name) }.map { it.toAttribute() }
