package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import uk.ac.ebi.biostd.extended.model.ExtSection
import uk.ac.ebi.biostd.extended.model.ExtSectionTable

internal fun DocSectionTableRow.toExtSection(): ExtSection =
    ExtSection(
        accNo = accNo,
        type = type,
        attributes = attributes.map { it.toExtAttribute() },
    )

internal fun DocSectionTable.toExtSectionTable(): ExtSectionTable = ExtSectionTable(sections.map { it.toExtSection() })
