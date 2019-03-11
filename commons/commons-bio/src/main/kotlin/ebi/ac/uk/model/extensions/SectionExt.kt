package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.Section

fun Section.allFiles() = files.map { it.fold({ file -> listOf(file) }, { table -> table.elements }) }.flatten()

fun Section.allReferencedFiles() = libraryFile?.referencedFiles?.toList() ?: listOf()

fun Section.allSections() =
    sections.map { it.fold({ section -> listOf(section) }, { table -> table.elements }) }.flatten()
