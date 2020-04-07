package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.DbAttributeMapper.toAttributes
import ac.uk.ebi.biostd.persistence.mapping.DbEitherMapper.toExtendedSections
import ac.uk.ebi.biostd.persistence.mapping.DbEitherMapper.toFiles
import ac.uk.ebi.biostd.persistence.mapping.DbEitherMapper.toLinks
import ac.uk.ebi.biostd.persistence.mapping.DbEitherMapper.toSections
import ac.uk.ebi.biostd.persistence.mapping.DbEntityMapper.toFileList
import ac.uk.ebi.biostd.persistence.mapping.DbEntityMapper.toUser
import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.SubmissionDb
import ac.uk.ebi.biostd.persistence.model.Tabular
import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.ExtendedSection
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.constants.SubFields.RELEASE_DATE
import ebi.ac.uk.model.constants.SubFields.TITLE
import ac.uk.ebi.biostd.persistence.model.AttributeDetail as AttributeDetailDb
import ac.uk.ebi.biostd.persistence.model.DbAttribute as AttributeDb
import ac.uk.ebi.biostd.persistence.model.DbFile as FileDb
import ac.uk.ebi.biostd.persistence.model.DbLink as LinkDb
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile as ReferencedFileDb
import ac.uk.ebi.biostd.persistence.model.DbSection as SectionDb
import ac.uk.ebi.biostd.persistence.model.DbUser as UserDb
import ac.uk.ebi.biostd.persistence.model.ReferencedFileList as FileListDb

class SubmissionDbMapper {
    private val sectionMapper = DbSectionMapper()

    fun toExtSubmission(submissionDb: SubmissionDb) =
        ExtendedSubmission(submissionDb.accNo, toUser(submissionDb.owner)).apply {
            version = submissionDb.version
            secretKey = submissionDb.secretKey
            relPath = submissionDb.relPath
            released = submissionDb.released
            method = submissionDb.method
            creationTime = submissionDb.creationTime
            modificationTime = submissionDb.modificationTime
            releaseTime = submissionDb.releaseTime
            section = sectionMapper.toSection(submissionDb.rootSection)
            processingStatus = submissionDb.status
            extendedSection = sectionMapper.toExtendedSection(submissionDb.rootSection)
            attributes = getAttributes(submissionDb)
            accessTags = submissionDb.accessTags.mapTo(mutableListOf(), DbAccessTag::name)
            tags = submissionDb.tags.mapTo(mutableListOf(), ::toTag)
        }

    private fun getAttributes(sub: SubmissionDb): List<Attribute> {
        val attrs = toAttributes(sub.attributes)
        sub.title?.also { title -> if (attrs.all { it.name != TITLE.value }) attrs.add(Attribute(TITLE.value, title)) }
        sub.rootPath?.also { attrs.add(Attribute(SubFields.ROOT_PATH.value, it)) }
        return attrs
    }

    fun toSubmission(submissionDb: SubmissionDb): Submission =
        Submission(
            accNo = submissionDb.accNo,
            attributes = getSubAttributes(submissionDb))
            .apply {
                accessTags = submissionDb.accessTags.mapTo(mutableListOf(), DbAccessTag::name)
                section = sectionMapper.toSection(submissionDb.rootSection)
            }

    private fun getSubAttributes(submissionDb: SubmissionDb): List<Attribute> {
        return when (val releaseTime = submissionDb.releaseTime) {
            null -> toAttributes(submissionDb.attributes)
            else -> toAttributes(submissionDb.attributes).plus(Attribute(RELEASE_DATE, releaseTime.toLocalDate()))
        }
    }

    private fun toTag(tag: DbTag) = Pair(tag.classifier, tag.name)
}

private class DbSectionMapper {
    internal fun toSection(sectionDb: SectionDb): Section =
        Section(accNo = sectionDb.accNo,
            type = sectionDb.type,
            links = toLinks(sectionDb.links.toList()),
            fileList = sectionDb.fileList?.let { toFileList(it) },
            files = toFiles(sectionDb.files.toList()),
            sections = toSections(sectionDb.sections.toList()),
            attributes = toAttributes(sectionDb.attributes))

    internal fun toExtendedSection(sectionDb: SectionDb) =
        ExtendedSection(sectionDb.type).apply {
            accNo = sectionDb.accNo
            links = toLinks(sectionDb.links.toList())
            files = toFiles(sectionDb.files.toList())
            sections = toSections(sectionDb.sections.toList())
            attributes = toAttributes(sectionDb.attributes)
            extendedSections = toExtendedSections(sectionDb.sections.toList())
            sectionDb.fileList?.let { fileList = toFileList(it) }
        }
}

private object DbEitherMapper {
    internal fun toLinks(links: List<LinkDb>) = toEitherList(links, DbEntityMapper::toLink, ::LinksTable)
    internal fun toFiles(files: List<FileDb>) = toEitherList(files, DbEntityMapper::toFile, ::FilesTable)

    internal fun toSections(sections: List<SectionDb>): MutableList<Either<Section, SectionsTable>> =
        toEitherList(sections, DbSectionMapper()::toSection, ::SectionsTable)

    internal fun toExtendedSections(sections: List<SectionDb>): MutableList<Either<ExtendedSection, SectionsTable>> =
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

    internal fun toFile(file: ReferencedFileDb) = File(file.name, file.size, toAttributes(file.attributes))
    internal fun toFileList(fileList: FileListDb) = FileList(fileList.name, fileList.files.map { toFile(it) })

    internal fun toUser(owner: UserDb) =
        User(owner.id, owner.email, owner.secret, owner.fullName, owner.notificationsEnabled)
}

private object DbAttributeMapper {
    internal fun toAttributes(attrs: Set<AttributeDb>): MutableList<Attribute> =
        attrs.mapTo(mutableListOf()) { toAttribute(it) }

    private fun toAttribute(attrDb: AttributeDb) =
        attrDb.run { Attribute(name, value, reference.orFalse(), toDetails(nameQualifier), toDetails(valueQualifier)) }

    private fun toDetails(details: MutableList<AttributeDetailDb>) =
        details.mapTo(mutableListOf()) { AttributeDetail(it.name, it.value) }
}
