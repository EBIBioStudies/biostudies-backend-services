package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.File

internal const val TO_EXT_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtFileKt"

fun File.toExtFile(fileSource: FilesSource): ExtFile =
    NfsFile(path, fileSource.getFile(path), attributes.map { it.toExtAttribute() })
