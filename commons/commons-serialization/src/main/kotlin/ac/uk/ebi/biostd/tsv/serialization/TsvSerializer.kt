package ac.uk.ebi.biostd.tsv.serialization

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Header
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.constants.SectionFields.FILE_LIST
import ebi.ac.uk.model.constants.TableFields.FILES_TABLE
import ebi.ac.uk.model.constants.TableFields.LINKS_TABLE

class TsvSerializer {
    fun <T> serialize(element: T): String {
        val builder = TsvBuilder()

        when (element) {
            is Submission -> addSubmission(builder, element)
            is Section -> addSection(builder, element, element.parentAccNo)
            is BioFile -> addFile(builder, element)
            is Link -> addLink(builder, element)
            is FilesTable -> addTable(builder, element, FILES_TABLE.toString())
            is LinksTable -> addTable(builder, element, LINKS_TABLE.toString())
            is SectionsTable -> addTable(builder, element, getHeader(element))
        }

        return builder.toString()
    }

    private fun addSubmission(builder: TsvBuilder, submission: Submission) {
        builder.addSubAcc(submission.accNo)
        submission.attributes.forEach(builder::addAttr)
        addSection(builder, submission.section)
    }

    private fun addSection(builder: TsvBuilder, section: Section, parentAccNo: String? = null) {
        builder.addSeparator()
        builder.addSecDescriptor(section.type, section.accNo, parentAccNo)
        sectionAttributes(section).forEach(builder::addAttr)

        section.links.forEach { either ->
            either.fold({ addLink(builder, it) }, { addTable(builder, it, LINKS_TABLE.toString()) })
        }
        section.files.forEach { either ->
            either.fold({ addFile(builder, it) }, { addTable(builder, it, FILES_TABLE.toString()) })
        }
        section.sections.forEach { either ->
            either.fold(
                { addSection(builder, it, section.accNo) },
                { addTable(builder, it, getHeader(it, section.accNo)) }
            )
        }
    }

    private fun sectionAttributes(section: Section): List<Attribute> = when (val fileList = section.fileList) {
        null -> section.attributes
        else -> section.attributes.plus(Attribute(FILE_LIST.value, "${fileList.name}.tsv"))
    }

    private fun getHeader(table: SectionsTable, parentAccNo: String? = null) =
        "${table.elements.first().type}[${if (parentAccNo.isNotBlank()) "$parentAccNo" else ""}]"

    private fun addFile(builder: TsvBuilder, file: BioFile) {
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
        builder.addSeparator()

        val headers = listOf(Header(mainHeader)) + table.headers
        builder.addTableRow(
            headers.flatMap { header ->
                buildList {
                    add(header.name)
                    addAll(header.termNames.map { "($it)" })
                    addAll(header.termValues.map { "[$it]" })
                }
            }
        )
        table.rows.forEach { builder.addTableRow(it) }
    }
}
