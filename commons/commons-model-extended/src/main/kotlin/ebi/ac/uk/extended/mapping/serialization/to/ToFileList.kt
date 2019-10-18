package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.FileList

internal const val TO_LIBRARY_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.to.ToFileListKt"

fun ExtFileList.toFileList(): FileList = FileList(fileName, files.map { it.toFile() })
