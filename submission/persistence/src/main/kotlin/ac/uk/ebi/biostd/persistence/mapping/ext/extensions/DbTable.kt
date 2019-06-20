package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.Link
import ac.uk.ebi.biostd.persistence.model.Section
import ac.uk.ebi.biostd.persistence.model.ext.isTableElement
import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.util.collections.component1
import ebi.ac.uk.util.collections.component2
import ebi.ac.uk.utils.FilesSource
import java.util.SortedSet

internal fun SortedSet<Link>.toExtLinks(): List<Either<ExtLink, ExtLinkTable>> {
    return groupBy { it.isTableElement() }
        .mapValues { it.value.map { link -> link.toExtLink() } }
        .let { (tableLinks, links) -> links.map { left(it) }.plus(right(ExtLinkTable(tableLinks))) }
}

internal fun SortedSet<File>.toExtFiles(filesSource: FilesSource): List<Either<ExtFile, ExtFileTable>> {
    return groupBy { it.isTableElement() }
        .mapValues { it.value.map { File -> File.toExtFile(filesSource) } }
        .let { (tableFiles, Files) -> Files.map { left(it) }.plus(right(ExtFileTable(tableFiles))) }
}

internal fun SortedSet<Section>.toExtSection(filesSource: FilesSource): List<Either<ExtSection, ExtSectionTable>> {
    return groupBy { it.isTableElement() }
        .mapValues { it.value.map { Section -> Section.toExtSection(filesSource) } }
        .let { (tableSections, Sections) -> Sections.map { left(it) }.plus(right(ExtSectionTable(tableSections))) }
}
