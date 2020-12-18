package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbFileAttribute

val DbFile.validAttributes: List<DbFileAttribute>
    get() = attributes.filterNot { it.value.isBlank() }

internal fun DbFile.isTableElement() = tableIndex != NO_TABLE_INDEX
