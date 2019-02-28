package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.File
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SectionFields

fun Section.allFiles(): List<File> {
    return files.map { it.fold({ file -> listOf(file) }, { table -> table.elements }) }.flatten()
}

fun Section.allSections(): List<Section> {
    return sections.map { it.fold({ section -> listOf(section) }, { table -> table.elements }) }.flatten()
}

var Section.libraryFile: String?
    get() = this[SectionFields.LIB_FILE]
    set(value) {
        value?.let { this[SectionFields.LIB_FILE] = it }
    }
