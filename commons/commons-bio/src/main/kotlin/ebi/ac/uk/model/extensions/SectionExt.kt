package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.AUTHOR_TYPE
import ebi.ac.uk.model.constants.ORG_TYPES
import ebi.ac.uk.model.constants.SectionFields

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
