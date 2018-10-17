package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.mapping.common.TabularMapper
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.FilesTable
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.LinksTable
import ac.uk.ebi.biostd.submission.Section
import ac.uk.ebi.biostd.submission.SectionsTable
import ac.uk.ebi.biostd.submission.SimpleAttribute
import ac.uk.ebi.biostd.submission.Submission
import ebi.ac.uk.model.IAccessTag
import ebi.ac.uk.model.IAttribute
import ebi.ac.uk.model.IFile
import ebi.ac.uk.model.ILink
import ebi.ac.uk.model.ISection
import ebi.ac.uk.model.ISimpleAttribute
import ebi.ac.uk.model.ISubmission

class FromEntityMapper(private val tabularMapper: TabularMapper = TabularMapper()) {

    fun toSubmission(submission: ISubmission): Submission {
        return Submission().apply {
            accNo = submission.accNo
            accessTags = toAccessTags(submission.accessTags)
            rootPath = submission.rootPath
            title = submission.title
            rtime = submission.releaseTime.toEpochSecond()
            attributes = toAttributes(submission.attributes)
            section = toSection(submission.rootSection)
        }
    }

    private fun toSection(sectionDb: ISection): Section {
        return Section().apply {
            type = sectionDb.type
            accNo = sectionDb.accNo
            attributes = toAttributes(sectionDb.attributes)
            links = tabularMapper.mapTabular(sectionDb.links, ::toLink, ::LinksTable)
            files = tabularMapper.mapTabular(sectionDb.files, ::toFile, ::FilesTable)
            subsections = tabularMapper.mapTabular(sectionDb.sections, ::toSection, ::SectionsTable)
        }
    }

    private fun toLink(link: ILink) =
            Link().apply {
                url = link.url
                attributes = toAttributes(link.attributes)
            }

    private fun toFile(file: IFile) =
            File().apply {
                name = file.name
                attributes = toAttributes(file.attributes)
                size = file.size
            }


    private fun toAttr(attrDb: IAttribute) = attrDb.run { Attribute(name, value, reference, toSimpleAttr(attrDb.valueAttributes)) }
    private fun toSimpleAttr(valueAttributes: MutableList<ISimpleAttribute>) = valueAttributes.mapTo(mutableListOf()) { SimpleAttribute(it.name, it.value) }
    private fun toAttributes(attrs: Set<IAttribute>) = attrs.mapTo(mutableListOf()) { toAttr(it) }
    private fun toAccessTags(accessTag: Set<IAccessTag>) = accessTag.mapTo(mutableListOf(), IAccessTag::name)

}
