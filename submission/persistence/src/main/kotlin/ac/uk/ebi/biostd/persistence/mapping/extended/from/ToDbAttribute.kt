package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail

internal fun ExtAttribute.toDbAttribute(index: Int): DbAttribute =
    DbAttribute(name, value, index, reference.orFalse(), nameAttrs.toDbDetails(), valueAttrs.toDbDetails())

private fun ExtAttributeDetail.toDbDetail(): AttributeDetail = AttributeDetail(name, value)

private fun List<ExtAttributeDetail>.toDbDetails() = mapTo(mutableListOf()) { it.toDbDetail() }
