package ebi.ac.uk.model

import arrow.core.Either

class Section(
        var type: String = "",
        var accNo: String = "",
        var sections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
        var files: MutableList<Either<File, FilesTable>> = mutableListOf(),
        var links: MutableList<Either<Link, LinksTable>> = mutableListOf(),
        attributes: List<Attribute> = emptyList()) : Attributable(attributes) {


    fun addFile(file: File) = files.add(Either.Left(file))
    fun addLink(link: Link) = links.add(Either.Left(link))
    fun addSection(section: Section) = sections.add(Either.Left(section))

    fun addFilesTable(table: FilesTable) = files.add(Either.Right(table))
    fun addLinksTable(table: LinksTable) = links.add(Either.Right(table))
    fun addSectionTable(table: SectionsTable) = sections.add(Either.Right(table))
}