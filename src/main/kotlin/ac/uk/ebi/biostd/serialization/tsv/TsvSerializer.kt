package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.common.LinksTable
import ac.uk.ebi.biostd.common.SectionTable
import ac.uk.ebi.biostd.common.fold
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Section
import ac.uk.ebi.biostd.submission.Submission

class TsvSerializer {

    private val builder: TsvBuilder = TsvBuilder()

    fun serialize(submission: Submission): String {
        serializeSubmission(submission)
        submission.sections.forEach(::serializeSection)
        return builder.toString()
    }

    private fun serializeSection(section: Section) {
        builder.addSeparator()
        builder.addSecDescriptor(section.type, section.accNo)

        section.attrs.forEach(builder::addSecAttr)
        section.links.forEach { it.fold(::addLink, ::addTable) }


        section.sections.forEach { it.fold(::serializeSection, ::serializeSectionTable) }
    }

    private fun serializeSectionTable(table: SectionTable) {
        builder.addSeparator()
        builder.addTableRow(table.getHeaders())

        table.getRows().forEach { builder.addTableRow(it) }
    }

    private fun addLink(link: Link) {
        builder.addSeparator()
        builder.addSecLink(link)
        builder.addSecLinkAttributes(link.attrs)
    }

    private fun addTable(table: LinksTable) {
        builder.addSeparator()
        builder.addTableRow(table.getHeaders())

        table.getRows().forEach { builder.addTableRow(it) }
    }

    private fun serializeSubmission(submission: Submission) {
        builder.addSubAccAndTags(submission.accNo, submission.accessTags)
        builder.addSubTitle(submission.title)
        builder.addSubReleaseDate(submission.rTime)
        builder.addRootPath(submission.rootPath)
        submission.attributes.forEach(builder::addSecAttr)
    }
}
