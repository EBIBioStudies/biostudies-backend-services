package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.FileAttribute
import ac.uk.ebi.biostd.persistence.model.LinkAttribute
import ac.uk.ebi.biostd.persistence.model.SectionAttribute
import ac.uk.ebi.biostd.persistence.model.SubmissionAttribute
import ac.uk.ebi.biostd.persistence.repositories.TagsDataRepository
import arrow.core.Either
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.User
import java.util.SortedSet
import ac.uk.ebi.biostd.persistence.model.Attribute as AttributeDb
import ac.uk.ebi.biostd.persistence.model.AttributeDetail as AttributeDetailDb
import ac.uk.ebi.biostd.persistence.model.File as FileDb
import ac.uk.ebi.biostd.persistence.model.Link as LinkDb
import ac.uk.ebi.biostd.persistence.model.Section as SectionDb
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDb
import ac.uk.ebi.biostd.persistence.model.User as UserDb

class SubmissionMapper(private val tagsRepository: TagsDataRepository) {

    fun toSubmissionDb(submission: ExtendedSubmission) = SubmissionDb().apply {
        accNo = submission.accNo
        version = submission.version
        relPath = submission.relPath
        owner = toUser(submission.user)
        attributes = toAttributes(submission.attributes, ::SubmissionAttribute)
        accessTags = toAccessTag(submission.accessTags)
        rootSection = toSection(submission.section, NO_TABLE_INDEX)
    }

    private fun toUser(user: User) = UserDb(user.email, user.email, user.secretKey).apply { id = user.id }

    private fun toSection(section: Section, index: Int) = SectionDb(section.accNo, section.type).apply {
        order = index
        attributes = toAttributes(section.attributes, ::SectionAttribute)
        links = toLinks(section.links)
        files = toFiles(section.files)
        sections = toSections(section.sections)
    }

    private fun toSections(sections: MutableList<Either<Section, SectionsTable>>): SortedSet<SectionDb> {
        return sections.mapIndexed { index, either ->
            either.fold(
                { listOf(toSection(it, index)) },
                { it.elements.mapIndexed { tableIndex, file -> toTableSection(file, index + tableIndex, tableIndex) } })
        }.flatten().toSortedSet()
    }

    private fun toFiles(files: MutableList<Either<File, FilesTable>>) =
        files.mapIndexed { index, either ->
            either.fold(
                { listOf(toFile(it, index)) },
                { it.elements.mapIndexed { tableIndex, file -> toFile(file, index + tableIndex, tableIndex) } })
        }.flatten().toSortedSet()

    private fun toLinks(links: MutableList<Either<Link, LinksTable>>) =
        links.mapIndexed { index, either ->
            either.fold(
                { listOf(toLink(it, index)) },
                { it.elements.mapIndexed { tableIndex, link -> toLink(link, index + tableIndex, tableIndex) } })
        }.flatten().toSortedSet()

    private fun toTableSection(section: Section, index: Int, sectionTableIndex: Int) =
        SectionDb(section.accNo, section.type).apply {
            attributes = toAttributes(section.attributes, ::SectionAttribute)
            tableIndex = sectionTableIndex
            order = index
        }

    private fun toAccessTag(accessTags: List<String>) =
        accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it) }

    private fun toLink(link: Link, order: Int, tableIndex: Int = NO_TABLE_INDEX) =
        LinkDb(link.url, order, toAttributes(link.attributes, ::LinkAttribute), tableIndex)

    private fun toFile(file: File, order: Int, tableIndex: Int = NO_TABLE_INDEX) =
        FileDb(file.path, order, file.size, toAttributes(file.attributes, ::FileAttribute), tableIndex)

    private fun <F> toAttributes(attributes: List<Attribute>, build: (AttributeDb) -> F) =
        attributes.mapIndexedTo(sortedSetOf()) { index, order -> toAttribute(order, index, build) }

    private fun <F> toAttribute(attribute: Attribute, index: Int, build: (AttributeDb) -> F) =
        build(AttributeDb(
            attribute.name,
            attribute.value,
            index,
            attribute.reference.orFalse(),
            toAttributeDetails(attribute.nameAttrs),
            toAttributeDetails(attribute.valueAttrs)))

    private fun toAttributeDetails(details: MutableList<AttributeDetail>) =
        details.mapTo(mutableListOf()) { AttributeDetailDb(it.name, it.value) }
}
