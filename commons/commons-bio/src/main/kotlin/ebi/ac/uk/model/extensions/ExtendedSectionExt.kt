package ebi.ac.uk.model.extensions

import arrow.core.Either
import ebi.ac.uk.model.ExtendedSection
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.SectionFields
import ebi.ac.uk.util.collections.ifLeft

var ExtendedSection.libraryFileAttr: String?
    get() = this[SectionFields.LIB_FILE]
    set(value) {
        value?.let { this[SectionFields.LIB_FILE] = it }
    }

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

fun ExtendedSection.addReferencedFile(file: File) = libraryFile?.addFile(file)

fun ExtendedSection.asSection() = Section(type, accNo, toSections(), files, links, attributes)

private fun ExtendedSection.toSections(): MutableList<Either<Section, SectionsTable>> =
    extendedSections.mapTo(mutableListOf()) { either -> either.mapLeft { it.asSection() } }
