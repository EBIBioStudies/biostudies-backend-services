package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtLibraryFile
import ebi.ac.uk.model.LibraryFile

fun LibraryFile.toExtLibraryFile(fileSource: FilesSource): ExtLibraryFile =
    ExtLibraryFile(name, fileSource.getFile(name), referencedFiles.map { it.toExtFile(fileSource) })
