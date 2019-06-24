package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.FilesSource
import ebi.ac.uk.model.File

internal const val TO_EXT_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.from.ToExtFileKt"

fun File.toExtFile(fileSource: FilesSource): ExtFile =
    ExtFile(path, fileSource.getFile(path), attributes.map { it.toExtAttribute() })
