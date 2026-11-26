package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.model.LinksTable
import uk.ac.ebi.biostd.extended.model.ExtLinkTable

internal const val TO_EXT_TABLE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtTableKt"

fun LinksTable.toExtTable(): ExtLinkTable = ExtLinkTable(elements.map { it.toExtLink() })
