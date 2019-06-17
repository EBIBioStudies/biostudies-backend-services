package ac.uk.ebi.biostd.persistence.mapping.db.extensions

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.Section
import ac.uk.ebi.biostd.persistence.model.SectionAttribute
import ac.uk.ebi.biostd.persistence.model.Submission
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection

internal fun ExtSection.toRootDbSection(parent: Submission, index: Int): Section {
    val section = toDbSection(index)
    section.submission = parent
    return section
}

internal fun ExtSection.toDbSection(index: Int): Section {
    val section = asSection(index)
    section.tableIndex = NO_TABLE_INDEX
    section.sections = sections.toDbSections()
    section.libraryFile = libraryFile?.toDbLibraryFile()
    section.links = links.toDbLinks()
    section.files = files.toDbFiles()
    return section
}

internal fun ExtSection.toDbSection(index: Int, tableIndex: Int): Section {
    val section = asSection(index)
    section.tableIndex = tableIndex
    return section
}

private fun ExtSection.asSection(index: Int): Section {
    val section = Section(accNo, type)
    section.order = index
    section.attributes = attributes.mapIndexedTo(sortedSetOf(), ::asSectionAttribute)
    return section
}


private fun asSectionAttribute(index: Int, attr: ExtAttribute) = SectionAttribute(attr.toDbAttribute(index))
