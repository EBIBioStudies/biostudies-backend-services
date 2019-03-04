package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.AttributeMapper.toAttributes
import ac.uk.ebi.biostd.persistence.mapping.EntityMapper.toFile
import ac.uk.ebi.biostd.persistence.mapping.EntityMapper.toLink
import ac.uk.ebi.biostd.persistence.mapping.EntityMapper.toUser
import ac.uk.ebi.biostd.persistence.mapping.SectionMapper.toSection
import ac.uk.ebi.biostd.persistence.mapping.SectionMapper.toTableSection
import ac.uk.ebi.biostd.persistence.mapping.TableMapper.toFiles
import ac.uk.ebi.biostd.persistence.mapping.TableMapper.toLinks
import ac.uk.ebi.biostd.persistence.mapping.TableMapper.toSections
import ac.uk.ebi.biostd.persistence.model.FileAttribute
import ac.uk.ebi.biostd.persistence.model.LibraryFile
import ac.uk.ebi.biostd.persistence.model.LinkAttribute
import ac.uk.ebi.biostd.persistence.model.ReferencedFileAttribute
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
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ac.uk.ebi.biostd.persistence.model.Attribute as AttributeDb
import ac.uk.ebi.biostd.persistence.model.AttributeDetail as AttributeDetailDb
import ac.uk.ebi.biostd.persistence.model.File as FileDb
import ac.uk.ebi.biostd.persistence.model.Link as LinkDb
import ac.uk.ebi.biostd.persistence.model.ReferencedFile as ReferencedFileDb
import ac.uk.ebi.biostd.persistence.model.Section as SectionDb
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDb
import ac.uk.ebi.biostd.persistence.model.User as UserDb

class SubmissionMapper(private val tagsRepository: TagsDataRepository) {

    fun toSubmissionDb(submission: ExtendedSubmission) = SubmissionDb().apply {
        accNo = submission.accNo
        version = submission.version
        title = submission.title
        secretKey = submission.secretKey
        relPath = submission.relPath
        rootPath = submission.rootPath
        creationTime = submission.creationTime.toEpochSecond()
        modificationTime = submission.modificationTime.toEpochSecond()
        releaseTime = submission.releaseTime.toEpochSecond()

        owner = toUser(submission.user)
        rootSection = toSection(submission.section, NO_TABLE_INDEX)
        attributes = toAttributes(submission.attributes).mapTo(sortedSetOf(), ::SubmissionAttribute)
        accessTags = toAccessTag(submission.accessTags)
    }

    private fun toAccessTag(accessTags: List<String>) =
        accessTags.mapTo(mutableSetOf()) { tagsRepository.findByName(it) }
}

private object SectionMapper {

    fun toSection(section: Section, index: Int) = SectionDb(section.accNo, section.type).apply {
        order = index
        attributes = toAttributes(section.attributes).mapTo(sortedSetOf(), ::SectionAttribute)
        links = section.links.mapIndexed(::toLinks).flatten().toSortedSet()
        files = section.files.mapIndexed(::toFiles).flatten().toSortedSet()
        sections = section.sections.mapIndexed(::toSections).flatten().toSortedSet()

        section.libraryFile?.let { libraryFile = getLibraryFile(section) }
    }

    fun toTableSection(section: Section, index: Int, sectionTableIndex: Int) =
        SectionDb(section.accNo, section.type).apply {
            attributes = AttributeMapper.toAttributes(section.attributes).mapTo(sortedSetOf(), ::SectionAttribute)
            tableIndex = sectionTableIndex
            order = index
        }

    fun getLibraryFile(section: Section) = LibraryFile(section.libraryFile!!).apply {
        files = section.referencedFiles.mapTo(sortedSetOf(), EntityMapper::toRefFile)
    }
}

private object TableMapper {

    fun toSections(index: Int, either: Either<Section, SectionsTable>): List<SectionDb> =
        either.fold(
            { listOf(toSection(it, index)) },
            { it.elements.mapIndexed { tableIndex, file -> toTableSection(file, index + tableIndex, tableIndex) } })

    fun toFiles(index: Int, files: Either<File, FilesTable>) = files.fold(
        { listOf(toFile(it, index)) },
        { it.elements.mapIndexed { tableIndex, file -> toFile(file, index + tableIndex, tableIndex) } })

    fun toLinks(index: Int, links: Either<Link, LinksTable>) = links.fold(
        { listOf(toLink(it, index)) },
        { it.elements.mapIndexed { tableIndex, link -> toLink(link, index + tableIndex, tableIndex) } })
}

private object EntityMapper {

    fun toUser(user: User) = UserDb(user.id, user.email, user.email, user.secretKey)

    fun toLink(link: Link, order: Int, tableIndex: Int = NO_TABLE_INDEX) =
        LinkDb(link.url, order, toAttributes(link.attributes).mapTo(sortedSetOf(), ::LinkAttribute), tableIndex)

    fun toFile(file: File, order: Int, tableIndex: Int = NO_TABLE_INDEX) = FileDb(
        file.path, order, file.size, toAttributes(file.attributes).mapTo(sortedSetOf(), ::FileAttribute), tableIndex)

    fun toRefFile(file: File) = ReferencedFileDb(
        file.path, file.size, toAttributes(file.attributes).mapTo(sortedSetOf(), ::ReferencedFileAttribute))
}

private object AttributeMapper {

    internal fun toAttributes(attributes: List<Attribute>) =
        attributes.mapIndexedTo(sortedSetOf()) { index, order -> toAttribute(order, index) }

    private fun toAttribute(attr: Attribute, index: Int) = AttributeDb(
        attr.name, attr.value, index, attr.reference.orFalse(), toDetails(attr.nameAttrs), toDetails(attr.valueAttrs))

    private fun toDetails(details: List<AttributeDetail>) =
        details.mapTo(mutableListOf()) { AttributeDetailDb(it.name, it.value) }
}
