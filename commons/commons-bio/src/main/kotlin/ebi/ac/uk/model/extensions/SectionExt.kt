package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SectionFields

var Section.libraryFile: String?
    get() = this[SectionFields.LIB_FILE]
    set(value) {
        value?.let { this[SectionFields.LIB_FILE] = it }
    }

fun Section.allFiles() = files.map { it.fold({ file -> listOf(file) }, { table -> table.elements }) }.flatten()

fun Section.allSections() =
    sections.map { it.fold({ section -> listOf(section) }, { table -> table.elements }) }.flatten()
