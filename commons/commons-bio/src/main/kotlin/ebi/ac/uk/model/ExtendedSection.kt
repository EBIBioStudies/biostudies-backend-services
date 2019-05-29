package ebi.ac.uk.model

import arrow.core.Either
import java.util.Objects

class ExtendedSection(type: String) : Section(type) {

    var libraryFile: LibraryFile? = null
    var extendedSections: List<Either<ExtendedSection, SectionsTable>> = listOf()

    override fun equals(other: Any?) = when {
        other !is ExtendedSection -> false
        other === this -> true
        else -> Objects.equals(type, other.type)
            .and(Objects.equals(accNo, other.accNo))
            .and(Objects.equals(files, other.files))
            .and(Objects.equals(links, other.links))
            .and(Objects.equals(sections, other.sections))
            .and(Objects.equals(attributes, other.attributes))
            .and(Objects.equals(libraryFile, other.libraryFile))
            .and(Objects.equals(extendedSections, other.extendedSections))
    }

    override fun hashCode() =
        Objects.hash(type, accNo, files, links, sections, attributes, libraryFile, extendedSections)
}
