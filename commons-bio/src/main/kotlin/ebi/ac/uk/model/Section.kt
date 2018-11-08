package ebi.ac.uk.model

import arrow.core.Either
import ebi.ac.uk.util.collections.addLeft
import ebi.ac.uk.util.collections.addRight

class Section(
        var type: String = "",
        var accNo: String = "",
        var sections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
        var files: MutableList<Either<File, FilesTable>> = mutableListOf(),
        var links: MutableList<Either<Link, LinksTable>> = mutableListOf(),
        attributes: List<Attribute> = emptyList()) : Attributable(attributes) {


    fun addFile(file: File) = files.addLeft(file)
    fun addLink(link: Link) = links.addLeft(link)
    fun addSection(section: Section) = sections.addLeft(section)

    fun addFilesTable(table: FilesTable) = files.addRight(table)
    fun addLinksTable(table: LinksTable) = links.addRight(table)
    fun addSectionTable(table: SectionsTable) = sections.addRight(table)
}