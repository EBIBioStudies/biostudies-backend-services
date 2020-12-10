package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbLink
import ac.uk.ebi.biostd.persistence.model.DbSection
import ac.uk.ebi.biostd.persistence.model.Tabular
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
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.component1
import ebi.ac.uk.util.collections.component2
import java.util.SortedSet

internal fun SortedSet<DbLink>.toExtLinks(): List<Either<ExtLink, ExtLinkTable>> =
    groupBy { it.isTableElement() }
        .let { (tableLinks, links) -> asOrderedList(tableLinks, links) }
        .map { it.bimap({ it.toExtLink() }, { ExtLinkTable(it.map { it.toExtLink() }) }) }

internal fun SortedSet<DbFile>.toExtFiles(source: FilesSource): List<Either<ExtFile, ExtFileTable>> =
    groupBy { it.isTableElement() }
        .let { (tableFiles, files) -> asOrderedList(tableFiles, files) }
        .map { it.bimap({ it.toExtFile(source) }, { ExtFileTable(it.map { it.toExtFile(source) }) }) }

internal fun SortedSet<DbSection>.toExtSections(source: FilesSource): List<Either<ExtSection, ExtSectionTable>> =
    groupBy { it.isTableElement() }
        .let { (tableSections, sections) -> asOrderedList(tableSections, sections) }
        .map { it.bimap({ it.toExtSection(source) }, { ExtSectionTable(it.map { it.toExtSection(source) }) }) }

private fun <T : Tabular> asOrderedList(tableFiles: List<T>?, files: List<T>?): List<Either<T, List<T>>> =
    mutableListOf<Either<T, List<T>>>().apply {
        files?.forEach { add(left(it)) }
        tableFiles?.let { add(right(tableFiles)) }
        sortBy { either -> either.fold({ it.order }, { it.first().order }) }
    }
