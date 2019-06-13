package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SectionFields

var Section.libraryFileName: String?
    get() = this[SectionFields.LIB_FILE]
    set(value) {
        value?.let { this[SectionFields.LIB_FILE] = it }
    }

fun Section.allFiles() = files.map { either -> either.fold({ listOf(it) }, { it.elements }) }.flatten()
fun Section.allSections() = sections.map { either -> either.fold({ listOf(it) }, { it.elements }) }.flatten()
