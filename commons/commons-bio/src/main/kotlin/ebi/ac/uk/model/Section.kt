package ebi.ac.uk.model

import arrow.core.Either
import ebi.ac.uk.util.collections.addLeft
import ebi.ac.uk.util.collections.addRight
import java.util.Objects

@Suppress("LongParameterList")
class Section(
    var type: String = "",
    var accNo: String? = null,
    var fileList: FileList? = null,
    var sections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
    var files: MutableList<Either<File, FilesTable>> = mutableListOf(),
    var links: MutableList<Either<Link, LinksTable>> = mutableListOf(),
    var parentAccNo: String? = null,
    override var attributes: List<Attribute> = listOf()
) : Attributable {
    fun addFile(file: File) = files.addLeft(file)
    fun addLink(link: Link) = links.addLeft(link)
    fun addSection(section: Section) = sections.addLeft(section.apply { parentAccNo = this@Section.accNo })

    fun addFilesTable(table: FilesTable) = files.addRight(table)
    fun addLinksTable(table: LinksTable) = links.addRight(table)
    fun addSectionTable(table: SectionsTable) = sections.addRight(table)

    override fun equals(other: Any?) = when {
        other !is Section -> false
        other === this -> true
        else -> Objects.equals(type, other.type)
            .and(Objects.equals(accNo, other.accNo))
            .and(Objects.equals(attributes, other.attributes))
    }

    override fun hashCode() = Objects.hash(type, accNo, files, links, sections, attributes)
}
