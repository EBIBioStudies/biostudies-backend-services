package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbSection
import ac.uk.ebi.biostd.persistence.model.DbSectionAttribute
import ebi.ac.uk.model.constants.SectionFields

internal val DbSection.title: String?
    get() = attributes.find { it.name == SectionFields.TITLE.value }?.value

val DbSection.validAttributes: List<DbSectionAttribute>
    get() = attributes.filterNot { it.value.isBlank() }

internal fun DbSection.isTableElement() = tableIndex != NO_TABLE_INDEX
