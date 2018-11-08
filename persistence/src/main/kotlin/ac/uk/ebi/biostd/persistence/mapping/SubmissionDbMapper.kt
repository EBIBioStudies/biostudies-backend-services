package ac.uk.ebi.biostd.persistence.mapping

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.AttributeDb
import ac.uk.ebi.biostd.persistence.model.FileDb
import ac.uk.ebi.biostd.persistence.model.LinkDb
import ac.uk.ebi.biostd.persistence.model.SectionDb
import ac.uk.ebi.biostd.persistence.model.SubmissionDb
import ac.uk.ebi.biostd.persistence.model.Tabular
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.collections.groupByCondition

internal class SubmissionDbMapper {

    fun mapSubmission(submissionDb: SubmissionDb): Submission {
        return Submission(attributes = toAttributes(submissionDb.attributes)).apply {
            accNo = submissionDb.accNo
            accessTags = submissionDb.accessTags.mapTo(mutableListOf(), AccessTag::name)
            rootSection = toSection(submissionDb.rootSection)
        }
    }

    private fun toSection(sectionDb: SectionDb) =
            Section(links = toLinks(sectionDb.links.toList()),
                    files = toFiles(sectionDb.files.toList()),
                    sections = toSections(sectionDb.sections.toList()),
                    attributes = toAttributes(sectionDb.attributes))

    private fun toAttribute(attrDb: AttributeDb) = Attribute(attrDb.name, attrDb.value, attrDb.reference.orFalse())
    private fun toAttributes(attrs: Set<AttributeDb>) = attrs.mapTo(mutableListOf()) { toAttribute(it) }

    private fun toLinks(links: List<LinkDb>) = toEitherList(links, ::toLink, ::LinksTable)
    private fun toFiles(files: List<FileDb>) = toEitherList(files, ::toFile, ::FilesTable)
    private fun toSections(sections: List<SectionDb>) = toEitherList(sections, ::toSimpleSection, ::SectionsTable)

    private fun toLink(link: LinkDb) = Link(link.url, toAttributes(link.attributes))
    private fun toFile(file: FileDb) = File(file.name, toAttributes(file.attributes))
    private fun toSimpleSection(sectionDb: SectionDb) = Section(attributes = toAttributes(sectionDb.attributes))

    companion object EitherMapper {

        private fun areConsecutive(one: Tabular, another: Tabular) =
                one.order == (another.order) + 1 && one.tableIndex == another.tableIndex

        private fun <T : Tabular, S, U> toEitherList(elements: List<T>, transform: (T) -> S, tableBuilder: (List<S>) -> U) =
                elements.groupByCondition(EitherMapper::areConsecutive).mapTo(mutableListOf()) { mapGroup(it, transform, tableBuilder) }

        private fun <T, S, U> mapGroup(groups: List<T>, transform: (T) -> S, tableBuilder: (List<S>) -> U) =
                if (groups.size == 1) left(transform(groups.first())) else right(tableBuilder(groups.map { transform(it) }))
    }
}
