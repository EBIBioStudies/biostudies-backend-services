package ac.uk.ebi.biostd.persistence.mapping.ext

import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.ext.isTableElement
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.util.collections.component1
import ebi.ac.uk.util.collections.component2

internal fun toExtFile(file: File): ExtFile = ExtFile(file.name, file.size, toAttributes(file.attributes))

internal fun toExtFileList(files: Iterable<File>) = files
    .groupBy { it.isTableElement() }
    .mapValues { it.value.map(::toExtFile) }
    .let { (filesTable, file) -> file.map { Either.left(it) }.plus(Either.right(ExtFileTable(filesTable))) }
