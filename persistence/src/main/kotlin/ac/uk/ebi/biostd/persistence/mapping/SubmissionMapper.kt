package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.common.AttributeDb
import ac.uk.ebi.biostd.persistence.common.FileDb
import ac.uk.ebi.biostd.persistence.common.LinkDb
import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.common.SectionDb
import ac.uk.ebi.biostd.persistence.common.SubmissionDb
import ac.uk.ebi.biostd.persistence.model.FileAttribute
import ac.uk.ebi.biostd.persistence.model.LinkAttribute
import ac.uk.ebi.biostd.persistence.model.SectionAttribute
import ac.uk.ebi.biostd.persistence.model.SubmissionAttribute
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
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

internal class SubmissionMapper(private val tagsRepository: TagsDataRepository) {

    fun toSubmissionDb(submissionDb: Submission): SubmissionDb {
        return SubmissionDb().apply {
            accNo = submissionDb.accNo
            attributes = toAttributes(submissionDb.attributes, ::SubmissionAttribute)
            accessTags = toAccessTag(submissionDb.accessTags)
            rootSection = toSection(submissionDb.rootSection, NO_TABLE_INDEX)
        }
    }

    private fun toSection(section: Section, index: Int): SectionDb {
        return SectionDb(section.accNo, section.type).apply {
            order = index
            attributes = toAttributes(section.attributes, ::SectionAttribute)
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

    private fun toTableSection(section: Section, index: Int) =
            SectionDb(section.accNo, section.type).apply {
                attributes = toAttributes(section.attributes, ::SectionAttribute)
                order = index
            }

    private fun toAccessTag(accessTags: List<String>) = accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it) }

    private fun toLink(link: Link, order: Int) = LinkDb(link.url, order, toAttributes(link.attributes, ::LinkAttribute))
    private fun toFile(file: File, order: Int) = FileDb(file.name, order, toAttributes(file.attributes, ::FileAttribute))

    private fun <F> toAttributes(attributes: List<Attribute>, build: (AttributeDb) -> F) = attributes.mapIndexedTo(sortedSetOf()) { index, order -> toAttribute(order, index, build) }
    private fun <F> toAttribute(attribute: Attribute, index: Int, build: (AttributeDb) -> F) = build(AttributeDb(attribute.name, attribute.value, index, attribute.reference.orFalse()))
}