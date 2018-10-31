package ac.uk.ebi.biostd.tsv

import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.extensions.Section

class TsvSerializer {

    private val builder: TsvBuilder = TsvBuilder()

    fun serialize(submission: Submission): String {
        serializeSubmission(submission)
        serializeSection(submission.rootSection)
        return builder.toString()
    }

    private fun serializeSubmission(submission: Submission) {
        builder.addSubAccAndTags(submission.accNo, submission.accessTags)
        //  builder.addSubTitle(submission.title)
        //  builder.addSubReleaseDate(submission.rtime)
        //  builder.addRootPath(submission.rootPath)
        submission.attributes.forEach(builder::addAttr)
    }

    private fun serializeSection(section: Section) {
        builder.addSeparator()
        builder.addSecDescriptor(section.type, section.accNo)
        section.attributes.forEach(builder::addAttr)

        section.links.forEach { either -> either.fold(this::addLink) { addTable(it) } }
        section.files.forEach { either -> either.fold(this::addFile) { addTable(it) } }
        section.sections.forEach { either -> either.fold(this::serializeSection) { addTable(it) } }
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

    private fun <T : Any> addTable(table: Table<T>) {
        builder.addSeparator()
        builder.addTableRow(table.headers.flatMap { listOf(it.name) + it.termNames.map { "[$it]" } })

        table.rows.forEach { builder.addTableRow(it) }
    }
}
