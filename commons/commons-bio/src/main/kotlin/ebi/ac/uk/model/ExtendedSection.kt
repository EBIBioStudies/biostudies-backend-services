package ebi.ac.uk.model

import arrow.core.Either
import ebi.ac.uk.model.extensions.libraryFile
import ebi.ac.uk.util.collections.addLeft
import ebi.ac.uk.util.collections.addRight
import java.util.Objects

class ExtendedSection(type: String) : Section(type) {
    var libraryFile: LibraryFile? = null
    var extendedSections: MutableList<Either<ExtendedSection, SectionsTable>> = mutableListOf()

    constructor(section: Section) : this(section.type) {
        accNo = section.accNo
        files = section.files
        links = section.links
        sections = section.sections
        attributes = section.attributes

        section.libraryFile?.let { libraryFile = LibraryFile(it) }
        section.sections.forEach { sect ->
            sect.fold({ extendedSections.addLeft(ExtendedSection(it)) }, { extendedSections.addRight(it) })
        }
    }

    fun addReferencedFile(file: File) = libraryFile?.addFile(file)

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
