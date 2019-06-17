package ac.uk.ebi.biostd.persistence.mapping.db.extensions

import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.Link
import ac.uk.ebi.biostd.persistence.model.Section
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import java.util.SortedSet

fun List<Either<ExtLink, ExtLinkTable>>.toDbLinks(): SortedSet<Link> {
    val results = sortedSetOf<Link>()
    for ((index, link) in withIndex()) {
        link.fold(
            { results.add(it.toDbLink(index)) },
            { results.addAll(it.links.mapIndexed { tableIndex, link -> link.toDbLink(index + tableIndex, tableIndex) }) })
    }

    return results
}

fun List<Either<ExtFile, ExtFileTable>>.toDbFiles(): SortedSet<File> {
    val results = sortedSetOf<File>()
    for ((index, File) in withIndex()) {
        File.fold(
            { results.add(it.toDbFile(index)) },
            { results.addAll(it.files.mapIndexed { tableIndex, File -> File.toDbFile(index + tableIndex, tableIndex) }) })
    }

    return results
}

fun List<Either<ExtSection, ExtSectionTable>>.toDbSections(): SortedSet<Section> {
    val results = sortedSetOf<Section>()
    for ((index, Section) in withIndex()) {
        Section.fold(
            { results.add(it.toDbSection(index)) },
            { results.addAll(it.sections.mapIndexed { tableIndex, Section -> Section.toDbSection(index + tableIndex, tableIndex) }) })
    }

    return results
}



