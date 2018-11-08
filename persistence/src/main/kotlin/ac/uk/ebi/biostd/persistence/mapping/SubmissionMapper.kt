package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.model.AttributeDb
import ac.uk.ebi.biostd.persistence.model.FileDb
import ac.uk.ebi.biostd.persistence.model.LinkDb
import ac.uk.ebi.biostd.persistence.model.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.SectionDb
import ac.uk.ebi.biostd.persistence.model.SubmissionDb
import ac.uk.ebi.biostd.persistence.repositories.TagsRepository
import arrow.core.Either
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import java.util.*

class SubmissionMapper(private val tagsRepository: TagsRepository) {

    fun mapSubmission(submissionDb: Submission): SubmissionDb {
        return SubmissionDb().apply {
            accNo = submissionDb.accNo
            attributes = toAttributes(submissionDb.attributes)
            accessTags = toAccessTag(submissionDb.accessTags)
            rootSection = toSection(submissionDb.rootSection, NO_TABLE_INDEX)
        }
    }

    private fun toSection(section: Section, index: Int): SectionDb {
        return SectionDb().apply {
            order = index
            attributes = toAttributes(section.attributes)
            links = toLinks(section.links)
            files = toFiles(section.files)
            sections = toSections(section.sections)
        }
    }

    private fun toSections(sections: MutableList<Either<Section, SectionsTable>>): SortedSet<SectionDb> {
        return sections.mapIndexed { index, either ->
            either.fold(
                    { listOf(toSection(it, index)) },
                    { it.elements.mapIndexed { tableIndex, file -> toTableSection(file, index + tableIndex) } })
        }.flatten().toSortedSet()
    }

    private fun toFiles(files: MutableList<Either<File, FilesTable>>) =
            files.mapIndexed { index, either ->
                either.fold(
                        { listOf(toFile(it, index)) },
                        { it.elements.mapIndexed { tableIndex, file -> toFile(file, index + tableIndex) } })
            }.flatten().toSortedSet()

    private fun toLinks(links: MutableList<Either<Link, LinksTable>>) =
            links.mapIndexed { index, either ->
                either.fold(
                        { listOf(toLink(it, index)) },
                        { it.elements.mapIndexed { tableIndex, link -> toLink(link, index + tableIndex) } })
            }.flatten().toSortedSet()

    private fun toTableSection(section: Section, index: Int) = SectionDb().apply {
        attributes = toAttributes(section.attributes)
        order = index
    }

    private fun toAccessTag(accessTags: List<String>) = accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it) }

    private fun toLink(link: Link, order: Int) = LinkDb(link.url, toAttributes(link.attributes), order)
    private fun toFile(file: File, order: Int) = FileDb(file.name, toAttributes(file.attributes), order)

    private fun toAttributes(attributes: List<Attribute>) = attributes.mapIndexedTo(sortedSetOf()) { index, order -> toAttribute(order, index) }
    private fun toAttribute(attribute: Attribute, index: Int) = AttributeDb(attribute.name, attribute.value, index, attribute.reference.orFalse())
}