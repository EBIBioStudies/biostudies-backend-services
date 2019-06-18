package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtLibraryFile
import ebi.ac.uk.model.LibraryFile

fun ExtLibraryFile.toLibraryFile(): LibraryFile = LibraryFile(fileName, referencedFiles.map { it.toFile() })
