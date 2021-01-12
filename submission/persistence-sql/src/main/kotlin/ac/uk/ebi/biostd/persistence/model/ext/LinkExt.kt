package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbLink
import ac.uk.ebi.biostd.persistence.model.DbLinkAttribute

val DbLink.validAttributes: List<DbLinkAttribute>
    get() = attributes.filterNot { it.value.isBlank() }

internal fun DbLink.isTableElement() = tableIndex != NO_TABLE_INDEX
