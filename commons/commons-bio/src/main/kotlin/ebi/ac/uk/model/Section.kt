package ebi.ac.uk.model

import arrow.core.Either
import ebi.ac.uk.util.collections.addLeft
import ebi.ac.uk.util.collections.addRight
import java.util.Objects

class Section(
    var type: String = "",
    var accNo: String? = null,
    var sections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
    var files: MutableList<Either<File, FilesTable>> = mutableListOf(),
    var links: MutableList<Either<Link, LinksTable>> = mutableListOf(),
    attributes: List<Attribute> = emptyList()
) : Attributable(attributes) {
    var parentAccNo: String? = null
    var libraryFile: String? = null
    var referencedFiles: MutableList<File> = mutableListOf()

    fun addFile(file: File) = files.addLeft(file)
    fun addLink(link: Link) = links.addLeft(link)
    fun addSection(section: Section) = sections.addLeft(section)
    fun addFilesTable(table: FilesTable) = files.addRight(table)
    fun addLinksTable(table: LinksTable) = links.addRight(table)
    fun addReferencedFile(file: File) = referencedFiles.add(file)
    fun addSectionTable(table: SectionsTable) = sections.addRight(table)

    override fun equals(other: Any?) = when {
        other !is Section -> false
        other === this -> true
        else -> Objects.equals(type, other.type)
            .and(Objects.equals(accNo, other.accNo))
            .and(Objects.equals(files, other.files))
            .and(Objects.equals(links, other.links))
            .and(Objects.equals(sections, other.sections))
            .and(Objects.equals(attributes, other.attributes))
            .and(Objects.equals(libraryFile, other.libraryFile))
            .and(Objects.equals(referencedFiles, other.referencedFiles))
    }

    override fun hashCode() = Objects.hash(type, accNo, attributes)
}
