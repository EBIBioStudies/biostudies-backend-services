package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable

internal const val TO_EXT_TABLE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtTableKt"

suspend fun FilesTable.toExtTable(fileSource: FileSourcesList): ExtFileTable =
    ExtFileTable(elements.map { fileSource.getExtFile(it.path, it.type, it.attributes) })

fun LinksTable.toExtTable(): ExtLinkTable = ExtLinkTable(elements.map { it.toExtLink() })
