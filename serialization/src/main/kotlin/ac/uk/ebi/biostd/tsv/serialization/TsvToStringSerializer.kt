package ac.uk.ebi.biostd.tsv.serialization

import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table

class TsvToStringSerializer {

    fun serialize(submission: Submission): String {
        val builder = TsvBuilder()

        serializeSubmission(builder, submission)
        serializeSection(builder, submission.section)
        return builder.toString()
    }

    private fun serializeSubmission(builder: TsvBuilder, submission: Submission) {
        builder.addSubAccAndTags(submission.accNo, submission.accessTags)
        submission.attributes.forEach(builder::addAttr)
    }

    private fun serializeSection(builder: TsvBuilder, section: Section) {
        builder.addSeparator()
        builder.addSecDescriptor(section.type, section.accNo)
        section.attributes.forEach(builder::addAttr)

        section.links.forEach { either -> either.fold({ addLink(builder, it) }, { addTable(builder, it) }) }
        section.files.forEach { either -> either.fold({ addFile(builder, it) }, { addTable(builder, it) }) }
        section.sections.forEach { either -> either.fold({ serializeSection(builder, it) }) { addTable(builder, it) } }
    }

    private fun addFile(builder: TsvBuilder, file: File) {
        builder.addSeparator()
        builder.addSecFile(file)
        builder.addAttributes(file.attributes)
    }

    private fun addLink(builder: TsvBuilder, link: Link) {
        builder.addSeparator()
        builder.addSecLink(link)
        builder.addAttributes(link.attributes)
    }

    private fun <T : Any> addTable(builder: TsvBuilder, table: Table<T>) {
        builder.addSeparator()
        builder.addTableRow(table.headers.flatMap { listOf(it.name) + it.termNames.map { "[$it]" } })

        table.rows.forEach { builder.addTableRow(it) }
    }
}
