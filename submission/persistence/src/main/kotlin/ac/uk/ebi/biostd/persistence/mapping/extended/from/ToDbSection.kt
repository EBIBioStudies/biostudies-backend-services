package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbSection
import ac.uk.ebi.biostd.persistence.model.DbSectionAttribute
import ac.uk.ebi.biostd.persistence.model.SubmissionDb
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import java.util.SortedSet

internal fun ExtSection.toDbSection(submission: SubmissionDb, index: Int): DbSection {
    val section = asSection(submission, index)
    section.tableIndex = NO_TABLE_INDEX
    section.sections = sections.toDbSections(submission)
    section.fileList = fileList?.toDbFileList()
    section.links = links.toDbLinks()
    section.files = files.toDbFiles()
    return section
}

private fun ExtSection.toDbTableSection(submission: SubmissionDb, index: Int, tableIndex: Int): DbSection {
    val section = asSection(submission, index)
    section.tableIndex = tableIndex
    return section
}

private fun List<Either<ExtSection, ExtSectionTable>>.toDbSections(submission: SubmissionDb): SortedSet<DbSection> {
    var idx = 0
    val sections = sortedSetOf<DbSection>()

    forEach { either ->
        either.fold(
            { sections.add(it.toDbSection(submission, idx++)) },
            { it.sections.forEachIndexed { tIdx, sec -> sections.add(sec.toDbTableSection(submission, idx++, tIdx)) } }
        )
    }

    return sections
}

private fun ExtSection.asSection(submission: SubmissionDb, sectionIndex: Int): DbSection {
    val section = DbSection(accNo, type)
    section.submission = submission
    section.order = sectionIndex
    section.attributes = getAttributes(attributes)
    return section
}

private fun getAttributes(attributes: List<ExtAttribute>) =
    attributes.mapIndexedTo(sortedSetOf()) { idx, attr -> DbSectionAttribute(attr.toDbAttribute(idx)) }
