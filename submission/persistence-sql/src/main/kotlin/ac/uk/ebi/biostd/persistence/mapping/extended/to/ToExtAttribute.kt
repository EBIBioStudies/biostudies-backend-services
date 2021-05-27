package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail

internal const val TO_EXT_ATTRIBUTE_EXTENSIONS = "ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtAttributeKt"

internal fun DbAttribute.toExtAttribute(): ExtAttribute = ExtAttribute(
    name, value, reference, nameQualifier.map { it.toExtDetail() }, valueQualifier.map { it.toExtDetail() }
)

internal fun AttributeDetail.toExtDetail(): ExtAttributeDetail = ExtAttributeDetail(name, value)
