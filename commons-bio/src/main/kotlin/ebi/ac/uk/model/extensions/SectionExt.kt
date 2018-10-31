package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.File
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constans.FileFields
import ebi.ac.uk.model.constans.SectionFields

var Section.type: String
    get() = this[FileFields.TYPE]
    set(value) {
        this[FileFields.TYPE] = value
    }

fun Section.allFiles(): List<File> {
    return files.map { it.fold({ file -> listOf(file) }, { table -> table.elements }) }.flatten()
}

fun Section.allSections(): List<Section> {
    return sections.map { it.fold({ section -> listOf(section) }, { table -> table.elements }) }.flatten()
}

var Section.parentAccNo: String?
    get() = this[SectionFields.PARENT_ACC_NO]
    set(value) {
        value?.let { this[SectionFields.PARENT_ACC_NO] = value }
    }


