package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.FilesSource
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.SectionsTable

fun SectionsTable.toExtTable(fileSource: FilesSource): ExtSectionTable =
    ExtSectionTable(elements.map { it.toExtSection(fileSource) })

fun FilesTable.toExtTable(fileSource: FilesSource): ExtFileTable =
    ExtFileTable(elements.map { it.toExtFile(fileSource) })

fun LinksTable.toExtTable(): ExtLinkTable = ExtLinkTable(elements.map { it.toExtLink() })

internal const val TO_EXT_TABLE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.from.ToExtTableKt"
