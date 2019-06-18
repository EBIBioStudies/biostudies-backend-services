package ebi.ac.uk.model

import arrow.core.Either
import java.util.Objects

class ExtendedSection(type: String) : Section(type) {
    var extendedSections: MutableList<Either<ExtendedSection, SectionsTable>> = mutableListOf()

    constructor(section: Section) : this(section.type) {
        accNo = section.accNo
        files = section.files
        links = section.links
        libraryFile = section.libraryFile
        sections = section.sections
        attributes = section.attributes
        extendedSections =
            section.sections.mapTo(mutableListOf()) { subSection -> subSection.bimap({ ExtendedSection(it) }, { it }) }
    }

    fun asSection() = Section(type, accNo, libraryFile, toSections(), files, links, attributes)

    private fun toSections(): MutableList<Either<Section, SectionsTable>> =
        extendedSections.mapTo(mutableListOf()) { extSect -> extSect.bimap({ it.asSection() }, { it }) }

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
