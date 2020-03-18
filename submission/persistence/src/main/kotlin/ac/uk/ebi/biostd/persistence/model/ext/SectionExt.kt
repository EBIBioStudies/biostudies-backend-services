package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.Section
import ebi.ac.uk.model.constants.SectionFields

internal fun Section.isTableElement() = tableIndex != NO_TABLE_INDEX

internal val Section.title: String?
    get() = attributes.find { it.name == SectionFields.TITLE.value }?.value
