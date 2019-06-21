package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.SectionsTable

fun ExtSectionTable.toTable(): SectionsTable = SectionsTable(sections.map { section -> section.toSection() })
fun ExtFileTable.toTable(): FilesTable = FilesTable(files.map { file -> file.toFile() })
fun ExtLinkTable.toTable(): LinksTable = LinksTable(links.map { link -> link.toLink() })

internal const val TO_TABLE_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.to.ToTableKt"
