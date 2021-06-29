package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.FileList

internal const val TO_EXT_LIBRARY_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtFileListKt"

fun FileList.toExtFileList(fileSource: FilesSource): ExtFileList =
    ExtFileList(
        name.substringBeforeLast("."),
        referencedFiles.map { it.toExtFile(fileSource) }
    )
