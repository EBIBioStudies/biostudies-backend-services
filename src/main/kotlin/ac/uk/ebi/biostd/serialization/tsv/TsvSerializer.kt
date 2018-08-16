package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.common.Table
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Section
import ac.uk.ebi.biostd.submission.Submission

class TsvSerializer {

    private val builder: TsvBuilder = TsvBuilder()

    fun serialize(submission: Submission): String {
        serializeSubmission(submission)
        serializeSection(submission.section)
        return builder.toString()
    }

    private fun serializeSubmission(submission: Submission) {
        builder.addSubAccAndTags(submission.accNo, submission.accessTags)
        builder.addSubTitle(submission.title)
        builder.addSubReleaseDate(submission.rTime)
        builder.addRootPath(submission.rootPath)
        submission.attributes.forEach(builder::addSecAttr)
    }

    private fun serializeSection(section: Section) {
        builder.addSeparator()
        builder.addSecDescriptor(section.type, section.accNo)
        section.attributes.forEach(builder::addSecAttr)

        section.links.forEach { it.fold({ addLink(it) }, { addTable(it) }) }
        section.files.forEach { it.fold({ addFile(it) }, { addTable(it) }) }
        section.subsections.forEach { it.fold({ serializeSection(it) }, { addTable(it) }) }
    }

    private fun addFile(file: File) {
        builder.addSeparator()
        builder.addSecFile(file)
        builder.addAttributes(file.attributes)
    }

    private fun addLink(link: Link) {
        builder.addSeparator()
        builder.addSecLink(link)
        builder.addAttributes(link.attributes)
    }

    private fun <T> addTable(table: Table<T>) {
        builder.addSeparator()
        builder.addTableRow(table.headers.flatMap { listOf(it.name) + it.termNames.map { "[$it]" } })

        table.rows.forEach { builder.addTableRow(it) }
    }
}
