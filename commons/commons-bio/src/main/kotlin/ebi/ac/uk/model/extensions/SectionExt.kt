package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.ExtendedSection
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SectionFields
import ebi.ac.uk.util.collections.ifLeft

var Section.libraryFile: String?
    get() = this[SectionFields.LIB_FILE]
    set(value) {
        value?.let { this[SectionFields.LIB_FILE] = it }
    }

fun Section.allFiles() = files.map { it.fold({ file -> listOf(file) }, { table -> table.elements }) }.flatten()

fun Section.allSections() =
    sections.map { it.fold({ section -> listOf(section) }, { table -> table.elements }) }.flatten()

fun ExtendedSection.allReferencedFiles(): List<File> {
    val refFiles: MutableList<File> = mutableListOf()
    libraryFile?.let { refFiles.addAll(it.referencedFiles) }

    return refFiles.toList()
}

fun ExtendedSection.allExtendedSections(): List<ExtendedSection> {
    val allExtended: MutableList<ExtendedSection> = mutableListOf()
    extendedSections.forEach { it.ifLeft { section -> allExtended.add(section) } }

    return allExtended.toList()
}
