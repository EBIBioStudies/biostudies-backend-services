package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.FileFields.MD5

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(): BioFile =
    when (this) {
        is NfsFile -> BioFile(filePath, file.size(), type.value, toAttributes())
        is FireFile -> BioFile(filePath, size, type.value, toAttributes())
    }

private fun ExtFile.toAttributes() =
    attributes.mapTo(mutableListOf()) { it.toAttribute() }.apply { add(Attribute(MD5.value, md5)) }
