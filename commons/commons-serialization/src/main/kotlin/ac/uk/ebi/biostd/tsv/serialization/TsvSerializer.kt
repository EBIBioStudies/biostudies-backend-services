package ac.uk.ebi.biostd.tsv.serialization

import uk.ac.ebi.biostd.base.isNotBlank
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.FilesTable
import uk.ac.ebi.biostd.model.Header
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.LinksTable
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.SectionsTable
import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.model.Table
import uk.ac.ebi.biostd.model.constants.SectionFields.FILE_LIST
import uk.ac.ebi.biostd.model.constants.SectionFields.LINK_LIST
import uk.ac.ebi.biostd.model.constants.TableFields.FILES_TABLE
import uk.ac.ebi.biostd.model.constants.TableFields.LINKS_TABLE

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

    private fun addSubmission(
        builder: TsvBuilder,
        submission: Submission,
    ) {
        builder.addSubAcc(submission.accNo)
        submission.attributes.forEach(builder::addAttr)
        addSection(builder, submission.section)
    }

    private fun addSection(
        builder: TsvBuilder,
        section: Section,
        parentAccNo: String? = null,
    ) {
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
                { addTable(builder, it, getHeader(it, section.accNo)) },
            )
        }
    }

    private fun sectionAttributes(section: Section) =
        section.attributes.plus(
            listOfNotNull(
                section.fileList?.let { Attribute(FILE_LIST.value, "${it.name}.tsv") },
                section.linkList?.let { Attribute(LINK_LIST.value, "${it.name}.tsv") },
            ),
        )

    private fun getHeader(
        table: SectionsTable,
        parentAccNo: String? = null,
    ) = "${table.elements.first().type}[${if (parentAccNo.isNotBlank()) "$parentAccNo" else ""}]"

    private fun addFile(
        builder: TsvBuilder,
        file: BioFile,
    ) {
        builder.addSeparator()
        builder.addSecFile(file)
        builder.addAttributes(file.attributes)
    }

    private fun addLink(
        builder: TsvBuilder,
        link: Link,
    ) {
        builder.addSeparator()
        builder.addSecLink(link)
        builder.addAttributes(link.attributes)
    }

    private fun <T : Any> addTable(
        builder: TsvBuilder,
        table: Table<T>,
        mainHeader: String,
    ) {
        builder.addSeparator()

        val headers = listOf(Header(mainHeader)) + table.headers
        builder.addTableRow(
            headers.flatMap { header ->
                buildList {
                    add(header.name)
                    addAll(header.termNames.map { "($it)" })
                    addAll(header.termValues.map { "[$it]" })
                }
            },
        )
        table.rows.forEach { builder.addTableRow(it) }
    }
}
