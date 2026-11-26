package ebi.ac.uk.extended.mapping.to

import uk.ac.ebi.biostd.extended.model.ExtFileTable
import uk.ac.ebi.biostd.extended.model.ExtLinkTable
import uk.ac.ebi.biostd.model.FilesTable
import uk.ac.ebi.biostd.model.LinksTable

internal const val TO_TABLE_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToTableKt"

fun ExtFileTable.toTable(): FilesTable = FilesTable(files.map { file -> file.toFile() })

fun ExtLinkTable.toTable(): LinksTable = LinksTable(links.map { link -> link.toLink() })
