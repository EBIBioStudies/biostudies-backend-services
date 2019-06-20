package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SectionFields

var Section.fileListName: String?
    get() = this[SectionFields.FILE_LIST]
    set(value) {
        value?.let { this[SectionFields.FILE_LIST] = it }
    }

fun Section.allFiles() = files.map { either -> either.fold({ listOf(it) }, { it.elements }) }.flatten()
fun Section.allSections() = sections.map { either -> either.fold({ listOf(it) }, { it.elements }) }.flatten()
