package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.Section
import ac.uk.ebi.biostd.persistence.model.SectionAttribute
import ac.uk.ebi.biostd.persistence.model.Submission
import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import java.util.SortedSet

internal fun ExtSection.toDbSection(submission: Submission, index: Int): Section {
    val section = asSection(submission, index)
    section.tableIndex = NO_TABLE_INDEX
    section.sections = sections.toDbSections(submission)
    section.fileList = fileList?.toDbFileList()
    section.links = links.toDbLinks()
    section.files = files.toDbFiles()
    return section
}

private fun ExtSection.toDbTableSection(submission: Submission, index: Int, tableIndex: Int): Section {
    val section = asSection(submission, index)
    section.tableIndex = tableIndex
    return section
}

private fun List<Either<ExtSection, ExtSectionTable>>.toDbSections(submission: Submission): SortedSet<Section> {
    var idx = 0
    val sections = sortedSetOf<Section>()

    for (either in this) {
        when (either) {
            is Left ->
                sections.add(either.a.toDbSection(submission, idx++))
            is Right ->
                either.b.sections.forEachIndexed { tableIdx, section ->
                    sections.add(section.toDbTableSection(submission, idx++, tableIdx))
                }
        }
    }

    return sections
}

private fun ExtSection.asSection(submission: Submission, sectionIndex: Int): Section {
    val section = Section(accNo, type)
    section.submission = submission
    section.order = sectionIndex
    section.attributes = getAttributes(attributes)
    return section
}

private fun getAttributes(attributes: List<ExtAttribute>) =
    attributes.mapIndexedTo(sortedSetOf()) { idx, attr -> SectionAttribute(attr.toDbAttribute(idx)) }
