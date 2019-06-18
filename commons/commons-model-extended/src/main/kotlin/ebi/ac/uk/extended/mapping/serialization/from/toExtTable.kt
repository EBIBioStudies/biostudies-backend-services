package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.SectionsTable

fun SectionsTable.toExtTable(fileSource: FilesSource): ExtSectionTable =
    ExtSectionTable(elements.map { section -> section.toExtSection(fileSource) })

fun FilesTable.toExtTable(fileSource: FilesSource): ExtFileTable =
    ExtFileTable(elements.map { file -> file.toExtFile(fileSource) })

fun LinksTable.toExtTable(): ExtLinkTable = ExtLinkTable(elements.map { link -> link.toExtLink() })
