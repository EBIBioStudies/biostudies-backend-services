package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.ExtendedSection
import ebi.ac.uk.model.File
import ebi.ac.uk.util.collections.ifLeft

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
