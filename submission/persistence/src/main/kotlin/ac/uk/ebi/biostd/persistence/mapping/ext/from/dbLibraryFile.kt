package ac.uk.ebi.biostd.persistence.mapping.ext.from

import ac.uk.ebi.biostd.persistence.model.LibraryFile
import ebi.ac.uk.extended.model.ExtLibraryFile

fun ExtLibraryFile.toDbLibraryFile(): LibraryFile = LibraryFile(fileName)
    .apply { files = referencedFiles.mapTo(sortedSetOf()) { it.toRefFile() } }
