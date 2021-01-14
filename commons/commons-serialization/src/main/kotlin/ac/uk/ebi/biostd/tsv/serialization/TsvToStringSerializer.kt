package ac.uk.ebi.biostd.tsv.serialization

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Header
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.constants.TableFields.FILES_TABLE
import ebi.ac.uk.model.constants.TableFields.LINKS_TABLE
import ebi.ac.uk.model.extensions.fileListName

class TsvToStringSerializer {
    fun <T> serialize(element: T): String {
        val builder = TsvBuilder()

        when (element) {
            is Submission -> serializeSubmission(builder, element as Submission)
            is Section -> {
                val section = element as Section
                serializeSection(builder, section, section.parentAccNo)
            }
            is File -> addFile(builder, element as File)
            is Link -> addLink(builder, element as Link)
            is FilesTable -> addTable(builder, element, FILES_TABLE.toString())
            is LinksTable -> addTable(builder, element, LINKS_TABLE.toString())
            is SectionsTable -> addTable(builder, element, getHeader(element))
        }

        return builder.toString()
    }

    private fun serializeSubmission(builder: TsvBuilder, submission: Submission) {
        builder.addSubAcc(submission.accNo)
        submission.attributes.forEach(builder::addAttr)
        serializeSection(builder, submission.section)
    }

    private fun serializeSection(builder: TsvBuilder, section: Section, parentAccNo: String? = null) {
        builder.addSeparator()
        builder.addSecDescriptor(section.type, section.accNo, parentAccNo)
        addFileListExt(section)
        section.attributes.forEach(builder::addAttr)

        section.links.forEach {
            either -> either.fold({ addLink(builder, it) }, { addTable(builder, it, LINKS_TABLE.toString()) }) }
        section.files.forEach {
            either -> either.fold({ addFile(builder, it) }, { addTable(builder, it, FILES_TABLE.toString()) }) }
        section.sections.forEach {
            either -> either.fold(
                { serializeSection(builder, it, section.accNo) },
                { addTable(builder, it, getHeader(it, section.accNo)) })
        }
    }

    private fun addFileListExt(section: Section) =
        section.fileList?.let { section.fileListName = "${it.name}.pagetab.tsv" }

    private fun getHeader(table: SectionsTable, parentAccNo: String? = null) =
        "${table.elements.first().type}[${if (parentAccNo.isNotBlank()) "$parentAccNo" else ""}]"

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

    private fun <T : Any> addTable(builder: TsvBuilder, table: Table<T>, mainHeader: String) {
        val headers = listOf(Header(mainHeader)) + table.headers

        builder.addSeparator()
        builder.addTableRow(headers.flatMap { header ->
            listOf(header.name) + header.termNames.map { "($it)" } + header.termValues.map { "[$it]" } })

        table.rows.forEach { builder.addTableRow(it) }
    }
}
