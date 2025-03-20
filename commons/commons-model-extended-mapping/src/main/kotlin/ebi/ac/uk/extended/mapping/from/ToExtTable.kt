package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.model.LinksTable

internal const val TO_EXT_TABLE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtTableKt"

fun LinksTable.toExtTable(): ExtLinkTable = ExtLinkTable(elements.map { it.toExtLink() })
