package uk.ac.ebi.biostd.model.extensions

import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.constants.AUTHOR_TYPE
import uk.ac.ebi.biostd.model.constants.ORG_TYPES
import uk.ac.ebi.biostd.model.constants.SectionFields

val Section.title: String?
    get() = find(SectionFields.TITLE)

var Section.fileListName: String?
    get() = find(SectionFields.FILE_LIST)
    set(value) {
        value?.let { this[SectionFields.FILE_LIST] = it }
    }

var Section.linkListName: String?
    get() = find(SectionFields.LINK_LIST)
    set(value) {
        value?.let { this[SectionFields.LINK_LIST] = it }
    }

fun Section.isAuthor(): Boolean = type.equals(AUTHOR_TYPE, ignoreCase = true)

fun Section.isOrganization(): Boolean = ORG_TYPES.contains(type.lowercase())

fun Section.allFiles(): List<BioFile> = files.flatMap { either -> either.fold({ listOf(it) }, { it.elements }) }

fun Section.allSections(): List<Section> = sections.flatMap { either -> either.fold({ listOf(it) + it.allSections() }, { it.elements }) }
