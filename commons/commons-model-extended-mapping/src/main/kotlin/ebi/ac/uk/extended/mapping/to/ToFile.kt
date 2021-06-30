package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields.DIR_TYPE
import ebi.ac.uk.model.constants.FileFields.FILE_TYPE

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(): File =
    when (this) {
        is NfsFile -> File(fileName, file.size(), type, attributes.mapTo(mutableListOf()) { it.toAttribute() })
        is FireFile -> TODO()
    }

private val ExtFile.type
    get() = when (this) {
        is NfsFile -> if (FileUtils.isDirectory(file)) DIR_TYPE.value else FILE_TYPE.value
        is FireFile -> TODO()
    }
