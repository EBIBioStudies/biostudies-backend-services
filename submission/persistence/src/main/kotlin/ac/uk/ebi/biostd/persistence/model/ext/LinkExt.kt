package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.Link

internal fun Link.isTableElement() = tableIndex != NO_TABLE_INDEX
