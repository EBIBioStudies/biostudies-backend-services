package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail

internal fun DocAttribute.toExtAttribute(): ExtAttribute = ExtAttribute(
    name, value, reference, nameAttrs.map { it.toExtDetail() }, valueAttrs.map { it.toExtDetail() })

internal fun DocAttributeDetail.toExtDetail(): ExtAttributeDetail = ExtAttributeDetail(name, value)
