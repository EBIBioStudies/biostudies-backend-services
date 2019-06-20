package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.LibraryFile
import ebi.ac.uk.extended.model.ExtLibraryFile
import ebi.ac.uk.utils.FilesSource

fun LibraryFile.toDbLibraryFile(fileSource: FilesSource): ExtLibraryFile =
    ExtLibraryFile(name, fileSource.getFile(name), files.map { it.toExtRefFile(fileSource) })
