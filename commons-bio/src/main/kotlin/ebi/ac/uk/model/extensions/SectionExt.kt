package ebi.ac.uk.model.extensions

import arrow.core.Either
import ebi.ac.uk.model.Attributable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constans.SectionFields

class Section(
        var sections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
        var files: MutableList<Either<File, FilesTable>> = mutableListOf(),
        var links: MutableList<Either<Link, LinksTable>> = mutableListOf(),
        attributes: List<Attribute> = emptyList()) : Attributable(attributes) {

    var accNo: String
        get() = this[SectionFields.ACC_NO]
        set(value) {
            this[SectionFields.ACC_NO] = value
        }

    var type: String
        get() = this[SectionFields.TYPE]
        set(value) {
            this[SectionFields.TYPE] = value
        }
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


