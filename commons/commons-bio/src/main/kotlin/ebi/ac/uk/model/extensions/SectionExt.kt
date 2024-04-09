package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SectionFields

var Section.fileListName: String?
    get() = find(SectionFields.FILE_LIST)
    set(value) {
        value?.let { this[SectionFields.FILE_LIST] = it }
    }

fun Section.allFiles(): List<BioFile> = files.flatMap { either -> either.fold({ listOf(it) }, { it.elements }) }

fun Section.allSections(): List<Section> = sections.flatMap { either -> either.fold({ listOf(it) + it.allSections() }, { it.elements }) }
