package uk.ac.ebi.biostd.extended.mapping.from

import uk.ac.ebi.biostd.extended.model.ExtLinkTable
import uk.ac.ebi.biostd.model.LinksTable

internal const val TO_EXT_TABLE_EXTENSIONS = "uk.ac.ebi.biostd.extended.mapping.from.ToExtTableKt"

fun LinksTable.toExtTable(): ExtLinkTable = ExtLinkTable(elements.map { it.toExtLink() })
