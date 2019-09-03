package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.Section

internal fun Section.isTableElement() = tableIndex != NO_TABLE_INDEX
