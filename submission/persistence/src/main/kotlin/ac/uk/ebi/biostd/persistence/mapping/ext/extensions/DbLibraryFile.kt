package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.LibraryFile
import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtLibraryFile

fun LibraryFile.toDbLibraryFile(fileSource: FilesSource): ExtLibraryFile =
    ExtLibraryFile(name, fileSource.get(name), files.map { it.toExtRefFile(fileSource) })
