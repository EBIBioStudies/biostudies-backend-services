package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.FileList

fun ExtFileList.toFileList(): FileList = FileList(fileName, referencedFiles.map { it.toFile() })

internal const val TO_LIBRARY_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.to.ToFileListKt"
