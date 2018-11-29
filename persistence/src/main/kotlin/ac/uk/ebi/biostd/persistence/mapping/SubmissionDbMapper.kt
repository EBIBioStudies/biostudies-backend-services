package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.common.AttributeDb
import ac.uk.ebi.biostd.persistence.common.AttributeDetailDb
import ac.uk.ebi.biostd.persistence.common.FileDb
import ac.uk.ebi.biostd.persistence.common.LinkDb
import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.common.SectionDb
import ac.uk.ebi.biostd.persistence.common.SubmissionDb
import ac.uk.ebi.biostd.persistence.common.UserDb
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Tabular
import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
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
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.util.collections.ifNotEmpty

class SubmissionDbMapper {

    fun toExtSubmission(submissionDb: SubmissionDb): ExtendedSubmission {
        return ExtendedSubmission(submissionDb.accNo, toUser(submissionDb.owner)).apply {
            attributes = toAttributes(submissionDb.attributes)
            accessTags = submissionDb.accessTags.mapTo(mutableListOf(), AccessTag::name)
            section = toSection(submissionDb.rootSection)
        }
    }

    private fun toUser(owner: UserDb) = User(owner.id, owner.email, owner.secret.orEmpty())

    fun toSubmission(submissionDb: SubmissionDb): Submission {
        return Submission(submissionDb.accNo, attributes = toAttributes(submissionDb.attributes)).apply {
            accessTags = submissionDb.accessTags.mapTo(mutableListOf(), AccessTag::name)
            section = toSection(submissionDb.rootSection)
        }
    }

    private fun toSection(sectionDb: SectionDb) =
        Section(accNo = sectionDb.accNo,
            type = sectionDb.type,
            links = toLinks(sectionDb.links.toList()),
            files = toFiles(sectionDb.files.toList()),
            sections = toSections(sectionDb.sections.toList()),
            attributes = toAttributes(sectionDb.attributes))

    private fun toAttribute(attrDb: AttributeDb) =
        Attribute(
            name = attrDb.name,
            value = attrDb.value,
            reference = attrDb.reference.orFalse(),
            nameAttrs = toDetails(attrDb.nameQualifier),
            valueAttrs = toDetails(attrDb.valueQualifier))

    private fun toDetails(details: MutableList<AttributeDetailDb>?): MutableList<AttributeDetail> =
        details.orEmpty().mapTo(mutableListOf()) { detail -> AttributeDetail(detail.name, detail.value) }

    private fun toAttributes(attrs: Set<AttributeDb>) = attrs.mapTo(mutableListOf()) { toAttribute(it) }

    private fun toLinks(links: List<LinkDb>) = toEitherList(links, ::toLink, ::LinksTable)
    private fun toFiles(files: List<FileDb>) = toEitherList(files, ::toFile, ::FilesTable)
    private fun toSections(sections: List<SectionDb>): MutableList<Either<Section, SectionsTable>> {
        return toEitherList(sections, ::toSection, ::SectionsTable)
    }

    private fun toLink(link: LinkDb) = Link(link.url, toAttributes(link.attributes))
    private fun toFile(file: FileDb) = File(file.name, toAttributes(file.attributes))

    companion object EitherMapper {

        private fun <T : Tabular, S, U> toEitherList(
            elements: List<T>,
            transform: (T) -> S,
            tableBuilder: (List<S>) -> U
        ):
            MutableList<Either<S, U>> {

            val map = elements.groupBy { it.tableIndex != NO_TABLE_INDEX }
            val eitherList = mutableListOf<Either<S, U>>()
            map[false].orEmpty().map { transform(it) }.forEach { eitherList.add(left(it)) }
            map[true].orEmpty().ifNotEmpty { listElements -> eitherList.add(right(tableBuilder(listElements.map { transform(it) }))) }
            return eitherList
        }
    }
}
