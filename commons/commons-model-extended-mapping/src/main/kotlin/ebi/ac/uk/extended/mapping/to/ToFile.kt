package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields.DIR_TYPE
import ebi.ac.uk.model.constants.FileFields.FILE_TYPE

internal const val TO_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileKt"

fun ExtFile.toFile(): File = File(fileName, file.size(), type, attributes.mapTo(mutableListOf()) { it.toAttribute() })

private val ExtFile.type
    get() = if (FileUtils.isDirectory(file)) DIR_TYPE.value else FILE_TYPE.value
