package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable

internal const val TO_LIBRARY_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToFileListKt"

fun ExtFileList.toFileList(): FileList = FileList(fileName, files.map { it.toFile() })

fun ExtFileList.toFilesTable() = FilesTable(toFileList().referencedFiles)
