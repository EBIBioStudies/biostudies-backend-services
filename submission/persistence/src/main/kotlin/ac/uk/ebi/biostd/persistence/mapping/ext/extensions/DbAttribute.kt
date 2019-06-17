package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.Attribute
import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail

internal fun Attribute.toExtAttribute(): ExtAttribute =
    ExtAttribute(name, value, reference, nameQualifier.toExtDetails(), valueQualifier.toExtDetails())

internal fun AttributeDetail.toExtDetail(): ExtAttributeDetail = ExtAttributeDetail(name, value)

private fun List<AttributeDetail>.toExtDetails() = map { it.toExtDetail() }

