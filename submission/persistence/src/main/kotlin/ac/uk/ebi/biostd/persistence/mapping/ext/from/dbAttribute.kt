package ac.uk.ebi.biostd.persistence.mapping.ext.from

import ac.uk.ebi.biostd.persistence.model.Attribute
import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail

internal fun ExtAttribute.toDbAttribute(index: Int): Attribute =
    Attribute(name, value, index, reference.orFalse(), nameAttrs.toDbDetails(), valueAttrs.toDbDetails())

private fun ExtAttributeDetail.toDbDetail(): AttributeDetail = AttributeDetail(name, value)

private fun List<ExtAttributeDetail>.toDbDetails() = map { it.toDbDetail() }
