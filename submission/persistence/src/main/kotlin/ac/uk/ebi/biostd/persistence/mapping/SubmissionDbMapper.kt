package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.DbAttributeMapper.toAttributes
import ac.uk.ebi.biostd.persistence.mapping.DbEitherMapper.toExtendedSections
import ac.uk.ebi.biostd.persistence.mapping.DbEitherMapper.toFiles
import ac.uk.ebi.biostd.persistence.mapping.DbEitherMapper.toLinks
import ac.uk.ebi.biostd.persistence.mapping.DbEitherMapper.toSections
import ac.uk.ebi.biostd.persistence.mapping.DbEntityMapper.toLibraryFile
import ac.uk.ebi.biostd.persistence.mapping.DbEntityMapper.toUser
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Tabular
import ac.uk.ebi.biostd.persistence.model.Tag
import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.functions.secondsToInstant
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.ExtendedSection
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import java.time.ZoneOffset.UTC
import ac.uk.ebi.biostd.persistence.model.Attribute as AttributeDb
import ac.uk.ebi.biostd.persistence.model.AttributeDetail as AttributeDetailDb
import ac.uk.ebi.biostd.persistence.model.File as FileDb
import ac.uk.ebi.biostd.persistence.model.LibraryFile as LibraryFileDb
import ac.uk.ebi.biostd.persistence.model.Link as LinkDb
import ac.uk.ebi.biostd.persistence.model.ReferencedFile as ReferencedFileDb
import ac.uk.ebi.biostd.persistence.model.Section as SectionDb
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDb
import ac.uk.ebi.biostd.persistence.model.User as UserDb

class SubmissionDbMapper {
    private val sectionMapper = DbSectionMapper()

    fun toExtSubmission(submissionDb: SubmissionDb, loadRefFiles: Boolean = false) =
        ExtendedSubmission(submissionDb.accNo, toUser(submissionDb.owner)).apply {
            version = submissionDb.version
            title = submissionDb.title
            secretKey = submissionDb.secretKey
            relPath = submissionDb.relPath
            rootPath = submissionDb.rootPath
            creationTime = toInstant(submissionDb.creationTime)
            modificationTime = toInstant(submissionDb.releaseTime)
            releaseTime = toInstant(submissionDb.releaseTime)

            section = sectionMapper.toSection(submissionDb.rootSection)
            extendedSection = sectionMapper.toExtendedSection(submissionDb.rootSection, loadRefFiles)
            attributes = toAttributes(submissionDb.attributes)
            accessTags = submissionDb.accessTags.mapTo(mutableListOf(), AccessTag::name)
            tags = submissionDb.tags.mapTo(mutableListOf(), ::toTag)
        }

    fun toSubmission(submissionDb: SubmissionDb) =
        Submission(submissionDb.accNo, attributes = toAttributes(submissionDb.attributes)).apply {
            accessTags = submissionDb.accessTags.mapTo(mutableListOf(), AccessTag::name)
            section = sectionMapper.toSection(submissionDb.rootSection)
        }

    private fun toInstant(dateSeconds: Long) = secondsToInstant(dateSeconds).atOffset(UTC)

    private fun toTag(tag: Tag) = Pair(tag.classifier, tag.name)
}

private class DbSectionMapper {
    internal fun toSection(sectionDb: SectionDb): Section =
        Section(accNo = sectionDb.accNo,
            type = sectionDb.type,
            links = toLinks(sectionDb.links.toList()),
            libraryFile = sectionDb.libraryFile?.let { toLibraryFile(it) },
            files = toFiles(sectionDb.files.toList()),
            sections = toSections(sectionDb.sections.toList()),
            attributes = toAttributes(sectionDb.attributes))

    internal fun toExtendedSection(sectionDb: SectionDb) = toExtendedSection(sectionDb, false)

    internal fun toExtendedSectionLoadFiles(sectionDb: SectionDb) = toExtendedSection(sectionDb, true)

    internal fun toExtendedSection(sectionDb: SectionDb, loadRefFiles: Boolean) =
        ExtendedSection(sectionDb.type).apply {
            accNo = sectionDb.accNo
            links = toLinks(sectionDb.links.toList())
            files = toFiles(sectionDb.files.toList())
            sections = toSections(sectionDb.sections.toList())
            attributes = toAttributes(sectionDb.attributes)
            extendedSections = toExtendedSections(sectionDb.sections.toList(), loadRefFiles)
            sectionDb.libraryFile?.let { libraryFile = toLibraryFile(it) }
        }
}

private object DbEitherMapper {
    internal fun toLinks(links: List<LinkDb>) = toEitherList(links, DbEntityMapper::toLink, ::LinksTable)
    internal fun toFiles(files: List<FileDb>) = toEitherList(files, DbEntityMapper::toFile, ::FilesTable)

    internal fun toSections(sections: List<SectionDb>) =
        toEitherList(sections, DbSectionMapper()::toSection, ::SectionsTable)

    internal fun toExtendedSections(
        sections: List<SectionDb>,
        loadRefFiles: Boolean
    ): MutableList<Either<ExtendedSection, SectionsTable>> =
        if (loadRefFiles)
            toEitherList(sections, DbSectionMapper()::toExtendedSectionLoadFiles, ::SectionsTable)
        else
            toEitherList(sections, DbSectionMapper()::toExtendedSection, ::SectionsTable)

    /**
     * Convert the given list of elements into an instance of @See [Either] using transform function for simple element
     * and table builder function for table types.
     */
    private fun <T : Tabular, S, U> toEitherList(
        elements: List<T>,
        transform: (T) -> S,
        tableBuilder: (List<S>) -> U
    ):
        MutableList<Either<S, U>> {

        val map = elements
            .groupBy { it.tableIndex != NO_TABLE_INDEX }
            .mapValues { entry -> entry.value.map(transform) }

        val eitherList = mutableListOf<Either<S, U>>()
        map[false]?.forEach { eitherList.add(left(it)) }
        map[true]?.let { eitherList.add(right(tableBuilder(it))) }
        return eitherList
    }
}

private object DbEntityMapper {
    internal fun toLink(link: LinkDb) = Link(link.url, toAttributes(link.attributes))
    internal fun toFile(file: FileDb) = File(file.name, file.size, toAttributes(file.attributes))

    internal fun toUser(owner: UserDb) = User(owner.id, owner.email, owner.secret)
    internal fun toFile(file: ReferencedFileDb) = File(file.name, file.size, toAttributes(file.attributes))
    internal fun toLibraryFile(libFile: LibraryFileDb) = LibraryFile(libFile.name, libFile.files.map { toFile(it) })
}

private object DbAttributeMapper {
    internal fun toAttributes(attrs: Set<AttributeDb>) = attrs.mapTo(mutableListOf()) { toAttribute(it) }

    private fun toAttribute(attrDb: AttributeDb) =
        attrDb.run { Attribute(name, value, reference.orFalse(), toDetails(nameQualifier), toDetails(valueQualifier)) }

    private fun toDetails(details: MutableList<AttributeDetailDb>) =
        details.mapTo(mutableListOf()) { AttributeDetail(it.name, it.value) }
}
