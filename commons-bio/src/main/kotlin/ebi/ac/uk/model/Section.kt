package ebi.ac.uk.model

import arrow.core.Either

class Section(
        var accNo: String = "",
        var sections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
        var files: MutableList<Either<File, FilesTable>> = mutableListOf(),
        var links: MutableList<Either<Link, LinksTable>> = mutableListOf(),
        attributes: List<Attribute> = emptyList()) : Attributable(attributes)