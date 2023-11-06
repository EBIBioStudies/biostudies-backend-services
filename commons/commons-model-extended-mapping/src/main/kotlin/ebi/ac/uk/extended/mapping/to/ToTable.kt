package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable

internal const val TO_TABLE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToTableKt"

fun ExtFileTable.toTable(): FilesTable = FilesTable(files.map { file -> file.toFile() })

fun ExtLinkTable.toTable(): LinksTable = LinksTable(links.map { link -> link.toLink() })
