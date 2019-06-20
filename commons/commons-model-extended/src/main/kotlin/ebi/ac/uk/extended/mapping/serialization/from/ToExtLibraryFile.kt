package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtLibraryFile
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.utils.FilesSource

fun LibraryFile.toExtLibraryFile(fileSource: FilesSource): ExtLibraryFile =
    ExtLibraryFile(name, fileSource.getFile(name), referencedFiles.map { it.toExtFile(fileSource) })

internal const val TO_EXT_LIBRARY_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.from.ToExtLibraryFileKt"
